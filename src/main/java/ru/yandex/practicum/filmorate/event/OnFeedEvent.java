package ru.yandex.practicum.filmorate.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.AllowedFeedEvents;

@AllArgsConstructor
@Data
public class OnFeedEvent {
    private int userId;
    private int entityId;
    private AllowedFeedEvents feedDetails;
}
