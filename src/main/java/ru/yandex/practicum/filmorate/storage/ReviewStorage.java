package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {
    Review addReview(Review review);
    Review getReview(Integer id);
    Review editReview(Review review);
    Integer removeReview(String id);
    Review addLike(Integer id);
    Review removeLike(Integer reviewId);
    Collection<Review> getAll(String filmId, int count);
}
