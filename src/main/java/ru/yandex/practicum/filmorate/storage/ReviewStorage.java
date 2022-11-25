package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {
    Review addReview(Review review);
    Review getReview(Integer id);
    Review editReview(Review review);
    Integer removeReview(String id);
    boolean removeLike(Integer reviewId, Integer userId);
    Collection<Review> getAll(String filmId, int count);
    boolean addLike(Integer reviewId, Integer userId);
    boolean addDislike(Integer reviewId, Integer userId);
    boolean removeDislike(Integer reviewId, Integer userId);
}
