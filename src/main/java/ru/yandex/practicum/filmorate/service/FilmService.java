package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.WrongIdException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.validator.GenreValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;

@Service
public class FilmService {
    private static int increment = 0;

    private final Validator validator;
    private final GenreStorage genreStorage;
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final DirectorStorage directorStorage;
    private final GenreValidator genreValidator;

    @Autowired
    public FilmService(Validator validator, GenreStorage genreStorage, @Qualifier("DBFilmStorage") FilmStorage filmStorage,
                       @Autowired(required = false) UserService userService, DirectorStorage directorStorage,
                       GenreValidator genreValidator) {
        this.validator = validator;
        this.genreStorage = genreStorage;
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.directorStorage = directorStorage;
        this.genreValidator = genreValidator;
    }

    /**
     * Возвращает коллекцию фильмов
     */
    public Collection<Film> getFilms() {
        final Collection<Film> films = filmStorage.getAllFilms();
        if (!films.isEmpty()) {
            addExtraFilmData(films);
        }
        return films;
    }

    /**
     * Добавляет фильм в коллекцию
     * Возвращает добавленный фильм
     *
     * @throws FilmValidationException в случае, если фильм содержит недопустимое содержание полей
     */
    public Film add(Film film) {
        validate(film);
        return filmStorage.addFilm(film);
    }

    /**
     * Обновляет фильм в коллекции
     * Возвращает обновленный фильм
     *
     * @throws FilmValidationException в случае, если фильм содержит недопустимое содержание полей
     */
    public Film update(Film film) {
        validate(film);
        return filmStorage.updateFilm(film);
    }

    /**
     * Добавляет лайк пользователя к фильму в коллекции
     */
    public void addLike(final String id, final String userId) {
        int storedFilmId = getStoredFilmId(id);
        int storedUserId = userService.getStoredUserId(userId);
        filmStorage.addLike(storedFilmId, storedUserId);
    }

    /**
     * Удаляет лайк пользователя к фильму в коллекции
     */
    public void deleteLike(final String id, final String userId) {
        int storedFilmId = getStoredFilmId(id);
        int storedUserId = userService.getStoredUserId(userId);
        filmStorage.deleteLike(storedFilmId, storedUserId);
    }

    /**
     * Возвращает коллекцию фильмов с наибольшим количеством лайков.
     *
     * @param count задает ограничение количества фильмов,
     *              если параметр не задан, будут возвращены первые 10 фильмов
     */
    public Collection<Film> getMostPopularFilms(final String count) {
        Integer size = intFromString(count);
        if (size == Integer.MIN_VALUE) {
            size = 10;
        }
        Collection<Film> films = filmStorage.getMostPopularFilms(size);
        addExtraFilmData(films);
        return films;
    }

    /**
     * Возвращает общую коллекцию фильмов для двух пользователей.
     * @param userId идентификатор первого пользователя
     * @param otherUserId идентификатор второго пользователя
     * */
    public Collection<Film> getCommonFilms(final String userId, final String otherUserId) {
        int storedUserId = userService.getStoredUserId(userId);
        int storedOtherUserId = userService.getStoredUserId(otherUserId);
        return filmStorage.getCommonFilms(storedUserId, storedOtherUserId);
    }

    /**
     * Возврашает фильм из коллекции по идентификатору
     *
     * @param id - идентификатор фильма
     * @throws WrongIdException  в случае, если программе не удастся распознать идентификатор
     * @throws NotFoundException в случае, если фильм по идентификатору отсутствует
     */
    public Film getFilm(String id) {
        return getStoredFilm(id);
    }

    public Collection<Film> getSortedFilmWithDirector(Integer id, String sortBy) {
        directorStorage.isExist(id);
        Collection<Film> films = filmStorage.getSortedFilmWithDirector(id, sortBy);
        addExtraFilmData(films);
        return films;
    }

    /**
     * Удаляет фильм из коллекции по идентификатору
     *
     * @param id - идентификатор фильма
     * @throws WrongIdException  в случае, если программе не удастся распознать идентификатор
     * @throws NotFoundException в случае, если фильм по идентификатору отсутствует
     */
    public void deleteFilm(String id) {
        int storedFilmId = getStoredFilmId(id);
        filmStorage.deleteFilm(storedFilmId);
    }

    private void validate(Film film) {
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        if (!violations.isEmpty()) {
            StringBuilder messageBuilder = new StringBuilder();
            for (ConstraintViolation<Film> filmConstraintViolation : violations) {
                messageBuilder.append(filmConstraintViolation.getMessage());
            }
            throw new FilmValidationException("Ошибка валидации Фильма: " + messageBuilder, violations);
        }
        if (film.getId() == 0) {
            film.setId(getNextId());
        }
    }

    private static int getNextId() {
        return ++increment;
    }

    private Integer intFromString(final String supposedInt) {
        try {
            return Integer.valueOf(supposedInt);
        } catch (NumberFormatException exception) {
            return Integer.MIN_VALUE;
        }
    }

    private int getIntFilmId(final String supposedId) {
        int id = intFromString(supposedId);
        if (id == Integer.MIN_VALUE) {
            throw new WrongIdException("Не удалось распознать идентификатор фильма: значение " + supposedId);
        }
        return id;
    }

    private void onFilmNotFound(int filmId) {
        throw new NotFoundException("Фильм с идентификатором " +
                filmId + " не зарегистрирован!");
    }

    public Film getStoredFilm(final String supposedId) {
        final int filmId = getIntFilmId(supposedId);

        Film film = filmStorage.getFilm(filmId);
        if (film == null) {
            onFilmNotFound(filmId);
        }
        addExtraFilmData(List.of(film));

        return film;
    }

    public int getStoredFilmId(final String supposedId) {
        final int filmId = getIntFilmId(supposedId);

        if (!filmStorage.containsFilm(filmId)) {
            onFilmNotFound(filmId);
        }
        return filmId;
    }

    public Collection<Film> getMostPopularFilms(String count, String genreId, String year) {
        Collection<Film> films;
        int checkedGenreID = getRequestedNumber(genreId);
        int checkedYear = getRequestedNumber(year);
        if (checkedGenreID != 0 && checkedYear != 0) {
            genreValidator.validateGenreById(checkedGenreID);
            films = filmStorage.getSortedByGenreAndYear(checkedGenreID,
                    checkedYear, Integer.parseInt(count));
        } else if (checkedGenreID != 0 && checkedYear == 0) {
            films = filmStorage.getMostPopularByGenre(Integer.parseInt(count), checkedGenreID);
        } else if (checkedGenreID == 0 && checkedYear != 0) {
            films = filmStorage.getMostPopularByYear(checkedYear, Integer.parseInt(count));
        } else {
            films = filmStorage.getMostPopularFilms(Integer.parseInt(count));
        }
        if (films.size() > 0) {
            addExtraFilmData(films);
        }
        return films;

    }

    protected void addExtraFilmData(Collection<Film> films) {
        genreStorage.load(films);
        directorStorage.load(films);
    }
    
    private int getRequestedNumber(String reqNum) {
        int result = 0;
        if (!reqNum.toLowerCase().equals("all")){
            try {
                result = Integer.parseInt(reqNum);
            } catch (NumberFormatException e){
                throw new WrongIdException("Не распознано значение параметра запроса");
            }
        }
        return result;
    }
}
