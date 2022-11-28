package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

public class ReviewStorageTestHelper {
    private final ReviewStorage storage;
    private int nextIdx = 0;

    public ReviewStorageTestHelper(ReviewStorage storage) {
        this.storage = storage;
    }

    public int getNewReviewId(int filmId, int userId, boolean isPositive) {
        return addReview(filmId, userId, isPositive).getReviewId();
    }

    public Review addReview(int filmId, int userId, boolean isPositive) {
        int idx = nextIdx++;

        return storage.add(
                Review.builder()
                        .content("content" + idx)
                        .filmId(filmId)
                        .userId(userId)
                        .isPositive(isPositive)
                        .build()
        );
    }
}