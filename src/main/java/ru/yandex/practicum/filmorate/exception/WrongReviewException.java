package ru.yandex.practicum.filmorate.exception;

import javax.validation.ValidationException;

public class WrongReviewException extends ValidationException {
    public WrongReviewException(String неверный_формат_данных_отзыва){
        super();
    }
}
