package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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

    public void requireReview(int reviewId) {
        if (!storage.containsReview(reviewId)) {
            throw new NotFoundException(
                    "Ревью с идентификатором " +
                    reviewId + " не зарегистрировано!"
            );
        }
    }

    public Collection<Review> getFilmReviews(String filmId, String count) {
        return storage.getAll(filmId, Integer.parseInt(count));
    }

    public boolean addLike(int reviewId, int userId) {
        return setScoreFromUser(reviewId, userId, true);
    }

    public boolean removeLike(int reviewId, int userId) {
        return unsetScoreFromUser(reviewId, userId, true);
    }

    public boolean addDislike(int reviewId, int userId) {
        return setScoreFromUser(reviewId, userId, false);
    }

    public boolean removeDislike(int reviewId, int userId) {
        return unsetScoreFromUser(reviewId, userId, false);
    }

    private boolean setScoreFromUser(int reviewId, int userId, boolean useful) {
        requireReview(reviewId);
        userService.requireUser(reviewId);

        if (storage.getScoreFromUser(reviewId, userId).orElse(!useful) == useful) {
            return false;
        }
        storage.setScoreFromUser(reviewId, userId, useful);
        return true;
    }

    private boolean unsetScoreFromUser(int reviewId, int userId, boolean useful) {
        requireReview(reviewId);
        userService.requireUser(reviewId);

        if (storage.getScoreFromUser(reviewId, userId).orElse(!useful) != useful) {
            return false;
        }
        storage.unsetScoreFromUser(reviewId, userId, useful);
        return true;
    }
}
