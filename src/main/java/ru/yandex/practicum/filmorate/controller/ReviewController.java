package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService service;


    @Autowired
    public ReviewController(ReviewService reviewService){
        this.service = reviewService;

    }

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review){
        return service.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review){
        return service.editReview(review);
    }

    @DeleteMapping("/{id}")
    public Integer deleteReview(@PathVariable String id){
        return service.removeReview(id);
    }

    @GetMapping("/{id}")
    public Review getReview(@PathVariable String id){
        return service.getReview(id);
    }

    @GetMapping
    public Collection<Review> getFilmReviews(@RequestParam(defaultValue = "all") String filmId,
                                            @RequestParam(defaultValue = "10") String count){
        return service.getAll(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public Review addReviewLike(@PathVariable String id, @PathVariable String userId){
        return service.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Review removeReviewLike(@PathVariable String id, @PathVariable String userId){
        return service.removeLike(Integer.parseInt(id), Integer.parseInt(userId));
    }

    @PutMapping("/{id}/dislike/{userId}")
    public Review addReviewDislike(@PathVariable String id, @PathVariable String userId){
        return service.addDislike(Integer.parseInt(id), Integer.parseInt(userId));
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public Review removeReviewDislike(@PathVariable String id, @PathVariable String userId){
        return service.addLike(Integer.parseInt(id), Integer.parseInt(userId));
    }










}
