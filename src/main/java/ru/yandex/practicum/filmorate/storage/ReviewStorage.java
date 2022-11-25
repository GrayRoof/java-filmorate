package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {
    Review addReview(Review review);
    Review getReview(Integer id);
    Review editReview(Review review);
    Integer removeReview(String id);
    Review removeLike(Integer reviewId, Integer userId);
    Collection<Review> getAll(String filmId, int count);
    Review addLike(Integer reviewId, Integer userId);
    Review addDislike(Integer reviewId, Integer userId);
    Review removeDislike(Integer reviewId, Integer userId);
}
