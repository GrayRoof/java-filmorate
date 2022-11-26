package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.util.Collection;

@Service
public class FeedService {
    private final UserService userService;
    private final FeedStorage feedStorage;

    public FeedService(UserService userService,
                       @Qualifier(UsedStorageConsts.QUALIFIER) FeedStorage feedStorage) {
        this.userService = userService;
        this.feedStorage = feedStorage;
    }

    /**
     * Возвращает коллекцию событий пользователя
     * @param supposedUserId - идентификатор пользователя
     * */
    public Collection<FeedEvent> getUserFeed(final String supposedUserId) {
        final int userId = userService.getStoredUserId(supposedUserId);
        return feedStorage.get(userId);
    }
}
