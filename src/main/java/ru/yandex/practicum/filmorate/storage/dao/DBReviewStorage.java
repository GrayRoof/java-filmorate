package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.event.OnDeleteUserEvent;
import ru.yandex.practicum.filmorate.event.OnFeedEvent;
import ru.yandex.practicum.filmorate.model.AllowedFeedEvents;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.*;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Component
@Qualifier(DBStorageConstants.QUALIFIER)
public class DBReviewStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public DBReviewStorage(JdbcTemplate jdbcTemplate, ApplicationEventPublisher eventPublisher){
        this.jdbcTemplate = jdbcTemplate;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Review add(Review review) {
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
        eventPublisher.publishEvent(new OnFeedEvent(review.getUserId(), id, AllowedFeedEvents.ADD_REVIEW));
        return get(id);
    }

    @Override
    public Review get(Integer id) {
        String sqlQuery = "select * from reviews where ReviewID = ?;";
        return jdbcTemplate.queryForObject(sqlQuery, (rs, rowNum) -> makeReview(rs), id);
    }

    @Override
    public Review update(Review review) {
        String sqlQuery = "update reviews set content = ?, isPositive = ? where ReviewID = ?;";
        boolean result = jdbcTemplate.update(sqlQuery,
                review.getContent(), review.getIsPositive(), review.getReviewId()) > 0;
        Review updatedReview = get(review.getReviewId());
        if (result) {
            eventPublisher.publishEvent(new OnFeedEvent(updatedReview.getUserId(), updatedReview.getReviewId(), AllowedFeedEvents.UPDATE_REVIEW));
        }
        return updatedReview;
    }

    @Override
    public Integer delete(String id) {
        Review review = get(Integer.parseInt(id));
        String sqlQuery = "delete from reviews where ReviewID = ?;";
        boolean result = jdbcTemplate.update(sqlQuery, id) > 0;
        if (result) {
            eventPublisher.publishEvent(new OnFeedEvent(review.getUserId(), review.getReviewId(), AllowedFeedEvents.REMOVE_REVIEW));
        }
        return Integer.parseInt(id);
    }

    @Override
    public Collection<Review> getAll(String filmId, int count) {
        String sqlQuery = "select * from reviews order by useful desc limit ?;";
        if (filmId.equals("all")) {
            return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeReview(rs), count);
        } else {
            sqlQuery = "select * from reviews where FilmID = ? order by useful desc limit ?;";
        }
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeReview(rs), filmId, count);
    }

    @Override
    public boolean contains(int reviewId) {
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
}