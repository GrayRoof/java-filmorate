package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.event.OnFeedEvent;
import ru.yandex.practicum.filmorate.model.AllowedFeedEvents;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.*;
import java.util.Collection;
import java.util.Objects;

@Component
@Qualifier(DBStorageConsts.QUALIFIER)
public class DBReviewStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public DBReviewStorage(JdbcTemplate jdbcTemplate, ApplicationEventPublisher eventPublisher){
        this.jdbcTemplate = jdbcTemplate;
        this.eventPublisher = eventPublisher;
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
        eventPublisher.publishEvent(new OnFeedEvent(review.getUserId(), id, AllowedFeedEvents.ADD_REVIEW));
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
        boolean result = jdbcTemplate.update(sqlQuery,
                review.getContent(), review.getIsPositive(), review.getReviewId()) > 0;
        Review updatedReview = getReview(review.getReviewId());
        if (result) {
            eventPublisher.publishEvent(new OnFeedEvent(updatedReview.getUserId(), updatedReview.getReviewId(), AllowedFeedEvents.UPDATE_REVIEW));
        }
        return updatedReview;
    }

    @Override
    public Integer removeReview(String id) {
        Review review = getReview(Integer.parseInt(id));
        String sqlQuery = "delete from reviews where ReviewID = ?;";
        boolean result = jdbcTemplate.update(sqlQuery, id) > 0;
        if (result) {
            eventPublisher.publishEvent(new OnFeedEvent(review.getUserId(), review.getReviewId(), AllowedFeedEvents.REMOVE_REVIEW));
        }
        return Integer.parseInt(id);
    }

    @Override
    public Review addLike(Integer id) {
        String sqlQuery = "update reviews set useful = ? where ReviewID = ?;";
        jdbcTemplate.update(sqlQuery, changeUsefulValue(id, true), id);
        return getReview(id);
    }


    @Override
    public Review removeLike(Integer reviewId) {
        String sqlQuery = "update reviews set useful = ? where ReviewID = ?;";
        jdbcTemplate.update(sqlQuery, changeUsefulValue(reviewId,false), reviewId);
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
}
