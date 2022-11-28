package ru.yandex.practicum.filmorate.exception;

import javax.validation.ValidationException;

public class FilmValidationInReviewException extends ValidationException {
    public FilmValidationInReviewException(String s) {
        super(s);
    }
}
