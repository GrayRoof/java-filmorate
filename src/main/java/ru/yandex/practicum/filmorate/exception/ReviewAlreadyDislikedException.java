package ru.yandex.practicum.filmorate.exception;

public class ReviewAlreadyDislikedException extends RuntimeException{
    public ReviewAlreadyDislikedException(String m){
        super(m);
    }
}
