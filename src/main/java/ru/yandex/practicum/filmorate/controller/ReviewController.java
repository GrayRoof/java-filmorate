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
        return service.add(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review){
        return service.update(review);
    }

    @DeleteMapping("/{id}")
    public Integer deleteReview(@PathVariable String id){
        return service.delete(id);
    }

    @GetMapping("/{id}")
    public Review getReview(@PathVariable String id){
        return service.get(id);
    }

    @GetMapping
    public Collection<Review> getFilmReviews(@RequestParam(defaultValue = "all") String filmId,
                                            @RequestParam(defaultValue = "10") String count){
        return service.getFilmReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public boolean addReviewLike(@PathVariable Integer id, @PathVariable Integer userId){
        return service.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public boolean removeReviewLike(@PathVariable Integer id, @PathVariable Integer userId){
        return service.removeLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public boolean addReviewDislike(@PathVariable Integer id, @PathVariable Integer userId){
        return service.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public boolean removeReviewDislike(@PathVariable Integer id, @PathVariable Integer userId){
        return service.addLike(id, userId);
    }










}
