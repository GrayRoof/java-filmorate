package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {
    Review addReview(Review review);
    Review getReview(Integer id);
    Review editReview(Review review);
    Integer removeReview(String id);
    Collection<Review> getAll(String filmId, int count);
    boolean containsReview(int reviewId);
    boolean setScoreFromUser(int reviewId, int userId, boolean useful);
    boolean unsetScoreFromUser(int reviewId, int userId, boolean useful);
}
