package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedEvent {
    private int id;
    private LocalDateTime timestamp = LocalDateTime.now();
    private int userId;
    private EventType eventType;
    private Operation operation;
    private int entityId;
}

