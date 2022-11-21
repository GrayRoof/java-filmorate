package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.dao.DBReviewStorage;

import java.util.Collection;

@Component
public class ReviewService {

    private final DBReviewStorage storage;

    private final UserService userService;

    private final FilmService filmService;


    @Autowired
    public ReviewService(DBReviewStorage reviewStorage, UserService userService,FilmService filmService){
        this.storage = reviewStorage;
        this.userService = userService;
        this.filmService = filmService;
    }
    public Review addReview(Review review) {
        userService.getUser(String.valueOf(review.getUserId()));
        filmService.getFilm(String.valueOf(review.getFilmId()));
        return storage.addReview(review);
    }

    public Review editReview(Review review) {
        userService.getUser(String.valueOf(review.getUserId()));
        filmService.getFilm(String.valueOf(review.getFilmId()));
        return storage.editReview(review);
    }

    public Integer removeReview(String id) {
        return storage.removeReview(id);
    }

    public Review getReview(String id) {
        return storage.getReview(Integer.parseInt(id));
    }

    public boolean addLike(String id, String userId) {
        userService.getUser(userId);
        return storage.addLike(Integer.parseInt(id));
    }

    public boolean removeLike(String id, String userId) {
        userService.getUser(userId);
        return storage.removeLike(Integer.parseInt(id));
    }

    public Collection<Review> getAll(String filmId, String count) {
        return storage.getAll(filmId, Integer.parseInt(count));
    }
}
