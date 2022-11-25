package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.*;
import java.util.Collection;
import java.util.Objects;

@Component
@Qualifier(DBStorageConsts.QUALIFIER)
public class DBReviewStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DBReviewStorage(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review addReview(Review review) {
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

        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();

        return getReview(id);
    }

    @Override
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

    @Override
    public Review editReview(Review review) {
        String sqlQuery = "update reviews set content = ?, isPositive = ? where ReviewID = ?;";
        jdbcTemplate.update(sqlQuery, review.getContent(), review.getIsPositive(), review.getReviewId());
        return getReview(review.getReviewId());
    }

    @Override
    public Integer removeReview(String id) {
        String sqlQuery = "delete from reviews where ReviewID = ?;";
        jdbcTemplate.update(sqlQuery, id);
        return Integer.parseInt(id);
    }

    @Override
    public Review addLike(Integer reviewId, Integer userId) {
        String sqlQuery = "update reviews set useful = ? where ReviewID = ?;";
        String sqlQuery2 = "insert into useful (reviewId, userId, useful) values (?, ?, ?)";

        jdbcTemplate.update(sqlQuery, changeUsefulValue(reviewId, true), reviewId);

        jdbcTemplate.update(sqlQuery2, reviewId, userId, true);
        return getReview(reviewId);
    }

    @Override
    public Review removeLike(Integer reviewId, Integer userId) {
        String sqlQuery = "update reviews set useful = ? where ReviewID = ?;";
        jdbcTemplate.update(sqlQuery, changeUsefulValue(reviewId,false), reviewId);
        String sqlQuery2 = "delete from useful where ReviewID = ? and userid = ? and useful = ?";
        jdbcTemplate.update(sqlQuery2, reviewId, userId, true);
        return getReview(reviewId);
    }

    private int changeUsefulValue(Integer reviewId, boolean increase){
        int useful = getReview(reviewId).getUseful();

        if (increase) {
            useful++;
        } else {
            useful--;
        }
        return useful;
    }

    @Override
    public Collection<Review> getAll(String filmId, int count) {
        String sqlQuery = "select * from reviews order by useful desc limit ?;";
        Integer id = 0;

        if (filmId.equals("all")) {
            return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeReview(rs), count);
        } else {
            sqlQuery = "select * from reviews where FilmID = ? order by useful desc limit ?;";
        }
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeReview(rs), filmId, count);

    }

    @Override
    public Review addDislike(Integer reviewId, Integer userId) {
        String sqlQuery = "update reviews set useful = ? where ReviewID = ?;";
        String sqlQuery2 = "insert into useful (reviewId, userId, useful) values (?, ?, ?)";

        jdbcTemplate.update(sqlQuery, changeUsefulValue(reviewId, false), reviewId);
        jdbcTemplate.update(sqlQuery2, reviewId, userId, false);

        return getReview(reviewId);
    }

    @Override
    public Review removeDislike(Integer reviewId, Integer userId) {
        String sqlQuery = "update reviews set useful = ? where ReviewID = ?;";
        jdbcTemplate.update(sqlQuery, changeUsefulValue(reviewId,true), reviewId);
        String sqlQuery2 = "delete from useful where ReviewID = ? and userid = ? and useful = ?";
        jdbcTemplate.update(sqlQuery2, reviewId, userId, false);
        return getReview(reviewId);
    }
}