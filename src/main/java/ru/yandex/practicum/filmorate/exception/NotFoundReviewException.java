package ru.yandex.practicum.filmorate.exception;

import javax.validation.ValidationException;

public class NotFoundReviewException extends ValidationException {
    public NotFoundReviewException(String m){
        super(m);
    }
}
