package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.validator.ReviewValidator;

import java.sql.*;
import java.util.Collection;
import java.util.Objects;

@Component
public class DBReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    private final ReviewValidator validator;

    @Autowired
    public DBReviewStorage(JdbcTemplate jdbcTemplate, ReviewValidator validator){
        this.jdbcTemplate = jdbcTemplate;
        this.validator = validator;
    }

    public Review addReview(Review review) {
//        validator.validateFilmById(review.getFilmId());
        String sqlQuery = "insert into reviews (content, isPositive, UserId, FilmID, Useful) values (?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setString(2, review.getIsPositive().toString());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            ps.setInt(5, review.getUseful());
            return ps;
        }, keyHolder);

//
//        simpleJdbcInsert.withTableName("reviews").usingGeneratedKeyColumns("ReviewID");
//
//        MapSqlParameterSource params = new MapSqlParameterSource()
//                .addValue("content", review.getContent())
//                .addValue("isPositive", review.getIsPositive())
//                .addValue("UserId", review.getUserId())
//                .addValue("FilmID", review.getFilmId())
//                .addValue("Useful", review.getUseful());
//
//        Number id = simpleJdbcInsert.executeAndReturnKey(params);

        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();

        return getReview(id);
    }

    public Review getReview(Integer id) {
        String sqlQuery = "select * from reviews where ReviewID = ?;";
        return jdbcTemplate.queryForObject(sqlQuery, (rs, rowNum) -> makeReview(rs), id);
    }

    private Review makeReview(ResultSet rs) throws SQLException {
        return Review.builder()
                .reviewId(rs.getInt("reviewId"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("isPositive"))
                .userId(rs.getInt("userId"))
                .filmId(rs.getInt("filmId"))
                .useful(rs.getInt("useful"))
                .build();
    }

    public Review editReview(Review review) {
        String sqlQuery = "update reviews set content = ?, isPositive = ? where ReviewID = ?;";
        jdbcTemplate.update(sqlQuery, review.getContent(), review.getIsPositive(), review.getReviewId());
        return getReview(review.getReviewId());
    }

    public Integer removeReview(String id) {
        String sqlQuery = "delete from reviews where ReviewID = ?;";
        jdbcTemplate.update(sqlQuery, id);
        return Integer.parseInt(id);
    }

    public Review addLike(Integer id) {
        Review review = getReview(id);
        Integer useful = review.getUseful();
        useful++;
        String sqlQuery = "update reviews set useful = ? where ReviewID = ?;";
        jdbcTemplate.update(sqlQuery, useful, id);
        return getReview(id);
    }


    public Review removeLike(Integer reviewId) {
        Review review = getReview(reviewId);
        int useful = review.getUseful();
        useful--;
        String sqlQuery = "update reviews set useful = ? where ReviewID = ?;";
        jdbcTemplate.update(sqlQuery, useful, reviewId);
        return getReview(reviewId);
    }

    public Collection<Review> getAll(String filmId, int count) {
        String sqlQuery = "select * from reviews order by useful desc limit ?;";
        Integer id = 0;

        if (filmId.equals("all")) {
            return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeReview(rs), count);
        } else {
            sqlQuery = "select * from reviews where FilmID = ? order by useful desc limit ?;";
//            id = Integer.valueOf(filmId);
        }

        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeReview(rs), filmId, count);

    }
}
