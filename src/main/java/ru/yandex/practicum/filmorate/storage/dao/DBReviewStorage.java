package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.event.OnDeleteUserEvent;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.*;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

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
    public boolean containsReview(int reviewId) {
        return jdbcTemplate.queryForRowSet(
                "select reviewid from reviews where reviewid = ?;",
                reviewId
        ).next();
    }

    @Override
    public Optional<Boolean> getScoreFromUser(int reviewId, int userId) {
        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(
                            "select useful from useful where reviewid=? and userid=?;",
                            Boolean.class,
                            reviewId,
                            userId
                    )
            );
        } catch(EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private void updateReviewUsefulness(int reviewId) {
        jdbcTemplate.update(
                "UPDATE reviews SET useful = ( " +
                "   SELECT " +
                "      COALESCE( " +
                "         SUM( " +
                "            CASE WHEN useful THEN " +
                "               1 " +
                "            ELSE " +
                "               -1 " +
                "            END " +
                "         ), " +
                "         0 " +
                "      ) " +
                "   FROM useful WHERE useful.reviewid = ? " +
                ") " +
                "WHERE reviewid = ?;",
                reviewId,
                reviewId
        );
    }

    @Override
    public void setScoreFromUser(int reviewId, int userId, boolean useful) {
        jdbcTemplate.update(
                "MERGE INTO useful (reviewid, userid, useful) KEY(reviewid, userid) VALUES (?, ?, ?);",
                reviewId,
                userId,
                useful
        );

        updateReviewUsefulness(reviewId);
    }

    @Override
    public void unsetScoreFromUser(int reviewId, int userId, boolean useful) {
        boolean done = jdbcTemplate.update(
                "DELETE FROM useful WHERE reviewid = ? AND userid = ? AND useful = ?;",
                reviewId,
                userId,
                useful
        ) > 0;

        if (done) {
            updateReviewUsefulness(reviewId);
        }
    }

    @EventListener
    public void handleOnDeleteUser(OnDeleteUserEvent event) {
        jdbcTemplate.update(
                "UPDATE reviews SET useful = ( " +
                "   SELECT " +
                "      COALESCE( " +
                "         SUM( " +
                "            CASE WHEN useful THEN " +
                "               1 " +
                "            ELSE " +
                "               -1 " +
                "            END " +
                "         ), " +
                "         0 " +
                "      ) " +
                "   FROM useful " +
                "   WHERE useful.reviewid=reviews.reviewid " +
                ");"
        );
    }
}