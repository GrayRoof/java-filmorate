package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedEvent {
    private int eventId;
    private long timestamp;
    private int userId;
    private EventType eventType;
    private Operation operation;
    private int entityId;
}

