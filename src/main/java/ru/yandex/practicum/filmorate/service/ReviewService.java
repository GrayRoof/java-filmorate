package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ReviewAlreadyDislikedException;
import ru.yandex.practicum.filmorate.exception.ReviewAlreadyLikedException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.validator.ReviewValidator;

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

    public Review addLike(String id, String userId) {
        try {
            validator.validateGoodReviewByUserAndId(Integer.valueOf(id), Integer.valueOf(userId));
            validator.validateBadReviewByUserAndId(Integer.valueOf(id), Integer.valueOf(userId));
        } catch (ReviewAlreadyLikedException e){
            return storage.removeLike(Integer.valueOf(id), Integer.valueOf(userId));
        } catch (ReviewAlreadyDislikedException e){
            storage.removeDislike(Integer.valueOf(id), Integer.valueOf(userId));
        }
        return storage.addLike(Integer.valueOf(id), Integer.valueOf(userId));
    }

    public Review removeLike(String id, String userId) {
        try{
            validator.validateGoodReviewByUserAndId(Integer.valueOf(id), Integer.valueOf(userId));
        } catch (ReviewAlreadyLikedException e){
            return null;
        }
        return storage.removeLike(Integer.valueOf(id), Integer.valueOf(userId));
    }

    public Collection<Review> getAll(String filmId, String count) {
        return storage.getAll(filmId, Integer.parseInt(count));
    }

    public Review addDislike(String id, String userId) {
        try {
            validator.validateBadReviewByUserAndId(Integer.parseInt(id), Integer.parseInt(userId));
            validator.validateGoodReviewByUserAndId(Integer.parseInt(id), Integer.parseInt(userId));
        } catch (ReviewAlreadyDislikedException e){
            return null;
        } catch (ReviewAlreadyLikedException e){
            storage.removeLike(Integer.parseInt(id), Integer.parseInt(userId));
            return storage.addDislike(Integer.parseInt(id), Integer.parseInt(userId));
        }
        return storage.addDislike(Integer.parseInt(id), Integer.parseInt(userId));
    }
}
