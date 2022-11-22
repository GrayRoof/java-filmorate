package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.dao.DBReviewStorage;
import ru.yandex.practicum.filmorate.validator.ReviewValidator;

import java.util.Collection;

@Component
public class ReviewService {

    private final DBReviewStorage storage;

    private final ReviewValidator validator;


    @Autowired
    public ReviewService(DBReviewStorage reviewStorage, ReviewValidator validator){
        this.storage = reviewStorage;
        this.validator = validator;
    }
    public Review addReview(Review review) {
//        validator.validateReview(review);
        validator.validateUserById(review.getUserId());
        return storage.addReview(review);
    }

    public Review editReview(Review review) {
//        validator.validateReview(review);
        return storage.editReview(review);
    }

    public Integer removeReview(String id) {
        return storage.removeReview(id);
    }

    public Review getReview(String id) {
        validator.validateReviewById(Integer.parseInt(id));
        return storage.getReview(Integer.parseInt(id));
    }

    public Review addLike(String id, String userId) {
        validator.validateUserById(Integer.parseInt(userId));
        return storage.addLike(Integer.parseInt(id));
    }

    public Review removeLike(String id, String userId) {
        return storage.removeLike(Integer.parseInt(id));
    }

    public Collection<Review> getAll(String filmId, String count) {
        return storage.getAll(filmId, Integer.parseInt(count));
    }
}
