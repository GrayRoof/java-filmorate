package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Set;

@Service
public class FilmService {
    private static int increment = 0;

    @Autowired
    private Validator validator;

    @Autowired
    private FilmDao filmDao;

    private void validate(Film film) {
        if (film.getId() == 0) {
            film.setId(++increment);
        }
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        if (!violations.isEmpty()) {
            StringBuilder messageBuilder = new StringBuilder();
            for (ConstraintViolation<Film> filmConstraintViolation : violations) {
                messageBuilder.append(filmConstraintViolation.getMessage());
            }
            throw new FilmValidationException("Ошибка валидации Фильма: " + messageBuilder, violations);
        }
    }

    public Collection<Film> getFilms() {
        return filmDao.getAllFilms();
    }

    public Film add(Film film) {
        validate(film);
       return filmDao.addFilm(film);
    }

    public Film update(Film film) {
        validate(film);
        if(!filmDao.getAllFilms().contains(film)) {
            throw new NotFoundException("Фильм с идентификатором " +
                    film.getId() + " не зарегистрирован!");
        }
        return filmDao.addFilm(film);
    }
}
