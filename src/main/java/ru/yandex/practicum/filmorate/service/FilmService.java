package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.WrongIdException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private static int increment = 0;

    private final Validator validator;

    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(Validator validator, FilmStorage filmStorage, UserService userService) {
        this.validator = validator;
        this.filmStorage = filmStorage;
        this.userService = userService;
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
            film.setId(++increment);
        }
    }

    public Collection<Film> getFilms() {
        return filmStorage.getAllFilms();
    }

    public Film add(Film film) {
        validate(film);
        return filmStorage.addFilm(film);
    }

    public Film update(Film film) {
        validate(film);
        return filmStorage.updateFilm(film);
    }

    public void addLike(final String id, final String userId) {
        Film film = getStoredFilm(id);
        User user = userService.getUser(userId);
        film.addLike(user.getId());
    }

    public void deleteLike(final String id, final String userId) {
        Film film = getStoredFilm(id);
        User user = userService.getUser(userId);
        film.deleteLike(user.getId());
    }

    public Collection<Film> getMostPopularFilms(final String count) {
        Integer size = intFromString(count);
        if (size == Integer.MIN_VALUE) {
            size = 10;
        }
        Collection<Film> films = filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
                .limit(size)
                .collect(Collectors.toCollection(HashSet::new));
        return films;
    }

    public Film getFilm(String id) {
        return getStoredFilm(id);
    }

    private Integer intFromString(final String supposedInt) {
        try {
            return Integer.valueOf(supposedInt);
        } catch (NumberFormatException exception) {
            return Integer.MIN_VALUE;
        }
    }

    private Film getStoredFilm(final String supposedId) {
        final int filmId = intFromString(supposedId);
        if (filmId == Integer.MIN_VALUE) {
            throw new WrongIdException("Не удалось распознать идентификатор фильма: " +
                    "значение " + supposedId);
        }
        Film film = filmStorage.getFilm(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с идентификатором " +
                    filmId + " не зарегистрирован!");
        }
        return film;
    }
}
