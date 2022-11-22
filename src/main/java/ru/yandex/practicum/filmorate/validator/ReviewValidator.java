package ru.yandex.practicum.filmorate.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.dao.DBReviewStorage;

import javax.validation.Valid;
import javax.validation.ValidationException;

@Component
public class ReviewValidator {

    private final DBReviewStorage storage;

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReviewValidator(DBReviewStorage storage, JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
        this.storage = storage;
    }

    public void validateReview(Review review){
        if (review.getContent().equals(null) || review.getContent().equals(null) ||
        review.getUserId().equals(null) ||
        review.getFilmId().equals(null)) throw new WrongReviewException("Неверный формат данных отзыва");
    }

    public void validateReviewById(Integer reviewId){
        String sqlQuery = "select count(*) from reviews where ReviewID = ?;";
        Integer id = jdbcTemplate.queryForObject(sqlQuery, Integer.class, reviewId);
        if (id != 1) throw new NotFoundException("Отзыва с таким ID не существует");
    }

    public void validateUserInReviewById(Integer reviewId, Integer userId){
        String sqlQuery = "select count(*) from reviews where ReviewID = ? and UserID = ?;";
        Integer id = jdbcTemplate.queryForObject(sqlQuery, Integer.class, reviewId, userId);
        if (id != 1) throw new WrongIdException("Пользователь с таким ID не оставлял отзыв к фильму");
    }

    public void validateUserById(Integer userId){
        String sqlQuery = "select count(*) from users where UserID = ?;";
        Integer id = jdbcTemplate.queryForObject(sqlQuery, Integer.class, userId);
        if (id.equals(0)) throw new NotFoundReviewException("Пользователя с таким ID не существует");
    }

    public void validateFilmById(Integer filmId){
        String sqlQuery = "select count(*) from films where FilmID = ?;";
        Integer id = jdbcTemplate.queryForObject(sqlQuery, Integer.class, filmId);
        if (id != 1) throw new FilmValidationInReviewException("Фильма с таким ID не существует");
    }

}
