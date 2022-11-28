package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {
    LIKE(1),
    REVIEW(2),
    FRIEND(3);
    private final int id;
}
