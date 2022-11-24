package ru.yandex.practicum.filmorate.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.*;

@Component
public class ReviewValidator {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReviewValidator(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public void validateReviewById(Integer reviewId){
        String sqlQuery = "select count(*) from reviews where ReviewID = ?;";
        Integer id = jdbcTemplate.queryForObject(sqlQuery, Integer.class, reviewId);
        if (id != 1) throw new NotFoundException("Отзыва с ID "+ reviewId +" не существует");
    }

    public void validateGoodReviewByUserAndId(Integer reviewId, Integer userId){
        String sqlQuery = "select count(*) from useful where ReviewID = ? and userId = ? and useful = ?";
        Integer id = jdbcTemplate.queryForObject(sqlQuery, Integer.class, reviewId, userId, true);
        if (id == 1) throw new ReviewAlreadyLikedException("Пользователь с ID "+ userId
                + "уже поставил лайк отзыву с ID " + reviewId);
    }

    public void validateBadReviewByUserAndId(Integer reviewId, Integer userId){
        String sqlQuery = "select count(*) from useful where ReviewID = ? and userId = ? and useful = ?";
        Integer id = jdbcTemplate.queryForObject(sqlQuery, Integer.class, reviewId, userId, false);
        if (id == 1) throw new ReviewAlreadyDislikedException("Пользователь с ID "+ userId
                + "уже поставил дизлайк отзыву с ID " + reviewId);
    }




}
