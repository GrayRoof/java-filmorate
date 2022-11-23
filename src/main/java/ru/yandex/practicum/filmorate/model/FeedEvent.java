package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedEvent {
    int id;
    LocalDateTime timestamp;
    int userId;
    EventType eventType;
    Operation operation;
    int entityId;
}

