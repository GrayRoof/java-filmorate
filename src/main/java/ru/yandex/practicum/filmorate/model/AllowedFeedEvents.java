package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AllowedFeedEvents {
    REMOVE_LIKE(Operation.REMOVE, EventType.LIKE),
    ADD_LIKE(Operation.ADD, EventType.LIKE),
    REMOVE_REVIEW(Operation.REMOVE, EventType.REVIEW),
    ADD_REVIEW(Operation.ADD, EventType.REVIEW),
    UPDATE_REVIEW(Operation.UPDATE, EventType.REVIEW),
    REMOVE_FRIEND(Operation.REMOVE, EventType.FRIEND),
    ADD_FRIEND(Operation.ADD, EventType.FRIEND);
    private final Operation operation;
    private final EventType type;


}
