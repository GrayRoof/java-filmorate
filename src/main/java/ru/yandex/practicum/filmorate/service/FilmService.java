package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.WrongIdException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.DBDirectorStorage;
import ru.yandex.practicum.filmorate.storage.dao.DBGenreStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;

@Service
public class FilmService {
    private static int increment = 0;

    private final Validator validator;
    private final DBGenreStorage dbGenreStorage;
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final DBDirectorStorage directorStorage;

    @Autowired
    public FilmService(Validator validator, DBGenreStorage dbGenreStorage, @Qualifier("DBFilmStorage") FilmStorage filmStorage,
                       @Autowired(required = false) UserService userService, DBDirectorStorage directorStorage) {
        this.validator = validator;
        this.dbGenreStorage = dbGenreStorage;
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.directorStorage = directorStorage;
    }

    /**
     * Возвращает коллекцию фильмов
     */
    public Collection<Film> getFilms() {
        final Collection<Film> films = filmStorage.getAllFilms();
        if (!films.isEmpty()) {
            dbGenreStorage.load(films);
            directorStorage.load(films);
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
        dbGenreStorage.load(films);
        directorStorage.load(films);
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
        dbGenreStorage.load(films);
        directorStorage.load(films);
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
        dbGenreStorage.load(List.of(film));
        directorStorage.load(List.of(film));
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
        int cnt = Integer.parseInt(count);
        int gId;
        int yr;
        try {
            gId = Integer.parseInt(genreId);
            yr = Integer.parseInt(year);
        } catch (NumberFormatException e){
            throw new NotFoundException("Неверный формат параметра запроса: "+e.getStackTrace());
        }

        return filmStorage.getMostPopularFilms(cnt,gId, yr);

    }
}
