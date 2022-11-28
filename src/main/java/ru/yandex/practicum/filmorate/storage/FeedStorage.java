package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.util.Collection;

public interface FeedStorage {
    Collection<FeedEvent> get(int userId);
}
