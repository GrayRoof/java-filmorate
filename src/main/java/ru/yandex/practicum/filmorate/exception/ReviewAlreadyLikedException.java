package ru.yandex.practicum.filmorate.exception;

public class ReviewAlreadyLikedException extends RuntimeException{
    public ReviewAlreadyLikedException(String m){
        super(m);
    }
}
