package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {
    Review addReview(Review review);
    Review getReview(Integer id);
    Review editReview(Review review);
    Integer removeReview(String id);
    Collection<Review> getAll(String filmId, int count);
    boolean containsReview(int reviewId);
    Optional<Boolean> getScoreFromUser(int reviewId, int userId);
    void setScoreFromUser(int reviewId, int userId, boolean useful);
    void unsetScoreFromUser(int reviewId, int userId, boolean useful);
}
