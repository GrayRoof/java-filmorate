package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.*;
import java.util.Objects;

@Component
public class DBReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DBReviewStorage(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public Review addReview(Review review) {
        String sqlQuery = "insert into reviews (content, isPositive, UserId, FilmID, Useful) values (?,?,?,?,?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.isPositive());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            ps.setInt(5, review.getUsefull());
            return ps;
        }, keyHolder);

        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        return getReview(id);
    }

    private Review getReview(int id) {
        String sqlQuery = "select * from reviews where id = ?;";
        return jdbcTemplate.queryForObject(sqlQuery, (rs, rowNum) -> makeReview(rs), id);
    }

    private Review makeReview(ResultSet rs) throws SQLException {
        return Review.builder()
                .id(rs.getInt("id"))
                .usefull(rs.getInt("useful"))
                .content(rs.getString("content"))
                .userId(rs.getInt("UserID"))
                .filmId(rs.getInt("FilmID"))
                .isPositive(rs.getBoolean("isPositive"))
                .build();
    }

    public Review editReview(Review review) {
        String sqlQuery = "update reviews set (id, useful, content, UserID, FilmID, isPositive) " +
                "where id = ?;";
        jdbcTemplate.update(sqlQuery, review.getId());
        return getReview(review.getId());
    }

    public Integer removeReview(String id) {
        String sqlQuery = "delete from reviews where id = ?;";
        jdbcTemplate.update(sqlQuery, id);
        return Integer.parseInt(id);

    }
}
