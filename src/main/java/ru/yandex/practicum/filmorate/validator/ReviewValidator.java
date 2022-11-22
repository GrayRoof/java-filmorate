package ru.yandex.practicum.filmorate.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Review;

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




}
