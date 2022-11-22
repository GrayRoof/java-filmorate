package ru.yandex.practicum.filmorate.exception;

public class WrongReviewException extends RuntimeException {
    public WrongReviewException(String msg){
        super(msg);
    }
}
