package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ReviewAlreadyDislikedException;
import ru.yandex.practicum.filmorate.exception.ReviewAlreadyLikedException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.validator.ReviewValidator;

import java.util.Collection;

@Component
public class ReviewService {

    private final ReviewStorage storage;

    private final ReviewValidator validator;

    private final FilmService filmService;

    private final UserService userService;


    @Autowired
    public ReviewService(
            UserService userService,
            FilmService filmService,
            ReviewValidator validator,
            @Qualifier(UsedStorageConsts.QUALIFIER) ReviewStorage storage
    ) {
        this.userService = userService;
        this.filmService = filmService;
        this.validator = validator;
        this.storage = storage;
    }
    public Review addReview(Review review) {
        userService.getStoredUserId(review.getUserId().toString());
        filmService.getStoredFilmId(review.getFilmId().toString());
        return storage.addReview(review);
    }

    public Review editReview(Review review) {
        return storage.editReview(review);
    }

    public Integer removeReview(String id) {
        return storage.removeReview(id);
    }

    public Review getReview(String id) {
        validator.validateReviewById(Integer.parseInt(id));
        return storage.getReview(Integer.parseInt(id));
    }

    public boolean addLike(int reviewId, int userId) {
        try {
            validator.validateGoodReviewByUserAndId(reviewId, userId);
            validator.validateBadReviewByUserAndId(reviewId, userId);
        } catch (ReviewAlreadyLikedException e){
            return storage.removeLike(reviewId, userId);
        } catch (ReviewAlreadyDislikedException e){
            return storage.removeDislike(reviewId, userId);
        }

        return storage.addLike(reviewId, userId);
    }

    public boolean removeLike(int reviewId, int userId) {
        try{
            validator.validateGoodReviewByUserAndId(reviewId, userId);
        } catch (ReviewAlreadyLikedException e){
            return storage.removeLike(reviewId, userId);
        }
        return false;
    }

    public Collection<Review> getFilmReviews(String filmId, String count) {
        return storage.getAll(filmId, Integer.parseInt(count));
    }

    public boolean addDislike(int reviewId, int userId) {
        try {
            validator.validateBadReviewByUserAndId(reviewId, userId);
            validator.validateGoodReviewByUserAndId(reviewId, userId);
        } catch (ReviewAlreadyDislikedException e){
            return false;
        } catch (ReviewAlreadyLikedException e){
            storage.removeLike(reviewId, userId);
            return storage.addDislike(reviewId, userId);
        }
        return storage.addDislike(reviewId, userId);
    }
}
