package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {
    Review add(Review review);
    Review get(Integer id);
    Review update(Review review);
    Integer delete(String id);
    Collection<Review> getAll(String filmId, int count);
    boolean contains(int reviewId);
    Optional<Boolean> getScoreFromUser(int reviewId, int userId);
    void setScoreFromUser(int reviewId, int userId, boolean useful);
    void unsetScoreFromUser(int reviewId, int userId, boolean useful);
}
