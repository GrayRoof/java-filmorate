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

    private final FilmService filmService;

    private final UserService userService;


    @Autowired
    public ReviewService(DBReviewStorage reviewStorage,
                         ReviewValidator validator,
                         FilmService filmService,
                         UserService userService){
        this.storage = reviewStorage;
        this.validator = validator;
        this.filmService = filmService;
        this.userService = userService;
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
        return storage.addLike(Integer.parseInt(id));
    }

    public Review removeLike(String id, String userId) {
        return storage.removeLike(Integer.parseInt(id));
    }

    public Collection<Review> getAll(String filmId, String count) {
        return storage.getAll(filmId, Integer.parseInt(count));
    }
}
