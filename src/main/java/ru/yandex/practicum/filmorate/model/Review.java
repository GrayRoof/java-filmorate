package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Review {
    private int id;
    private String content;
    private boolean isPositive;
    private int userId;
    private int filmId;
    private int useful;
}
