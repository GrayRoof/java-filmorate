package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/reviews")
public class ReviewController {

    @PostMapping
    public Review addReview(Review review){
        return null;
    }

    @PutMapping
    public Review editReview(Review review){
        return null;
    }

    @DeleteMapping("/{id}")
    public Integer deleteReview(@PathVariable String id){
        return null;
    }

    @GetMapping("/{id}")
    public Review getReview(@PathVariable String id){
        return null;
    }

    @GetMapping("?filmId={filmId}&count={count}")
    public Collection<Review> getFilmReviews(@RequestParam(defaultValue = "all") String filmId,
                                            @RequestParam(defaultValue = "10") String count){
        return null;
    }

    @PutMapping("/{id}/like/{userId}")
    public boolean addReviewLike(@PathVariable String id, @PathVariable String userId){
        return null;
    }

    @DeleteMapping("/{id}/like/{userId}")
    public boolean removeReviewLike(@PathVariable String id, @PathVariable String userId){
        return null;
    }

    @PutMapping("/{id}/dislike/{userId}")
    public boolean addReviewDislike(@PathVariable String id, @PathVariable String userId){
        return null;
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public boolean removeReviewDislike(@PathVariable String id, @PathVariable String userId){
        return null;
    }










}
