package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.RecommendationService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RecommendationService recommendationService;
    private final FeedService feedService;

    public UserController(@Autowired(required = false) UserService userService,
                          RecommendationService recommendationService,
                          FeedService feedService) {
        this.userService = userService;
        this.recommendationService = recommendationService;
        this.feedService = feedService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос GET к эндпоинту: /users");
        return userService.getAll();
    }

    @GetMapping("/{id}/friends")
    public Collection<User> findFriends(@PathVariable String id) {
        log.info("Получен запрос GET к эндпоинту: /users/{}/friends", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}")
    public User findUser(@PathVariable String id) {
        log.info("Получен запрос GET к эндпоинту: /users/{}/", id);
        return userService.get(id);
    }

    @GetMapping("/{id}/feed")
    public Collection<FeedEvent> findUserFeed(@PathVariable String id){
        log.info("Получен запрос GET к эндпоинту: /users/{}/feed", id);
        return feedService.getUserFeed(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> findCommonFriends(@PathVariable String id, @PathVariable String otherId) {
        log.info("Получен запрос GET к эндпоинту: /users/{}/friends/common/{}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Получен запрос POST. Данные тела запроса: {}", user);
        final User validUser = userService.add(user);
        log.info("Создан объект {} с идентификатором {}", User.class.getSimpleName(), validUser.getId());
        return validUser;
    }

    @PutMapping
    public User put(@RequestBody User user) {
        log.info("Получен запрос PUT. Данные тела запроса: {}", user);
        final User validUser = userService.update(user);
        log.info("Обновлен объект {} с идентификатором {}", User.class.getSimpleName(), validUser.getId());
        return validUser;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        log.info("Получен запрос DELETE к эндпоинту: users/{}", id);
        userService.delete(id);
        log.info("Удален объект {} с идентификатором {}",
                User.class.getSimpleName(), id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable String id, @PathVariable String friendId) {
        log.info("Получен запрос PUT к эндпоинту: /users/{}/friends/{}", id, friendId);
        //final User validUser = userService.update(user);
        userService.addFriend(id, friendId);
        log.info("Обновлен объект {} с идентификатором {}. Добавлен друг {}",
                User.class.getSimpleName(), id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable String id, @PathVariable String friendId) {
        log.info("Получен запрос DELETE к эндпоинту: /users/{}/friends/{}", id, friendId);
        userService.deleteFriend(id, friendId);
        log.info("Обновлен объект {} с идентификатором {}. Удален друг {}",
                User.class.getSimpleName(), id, friendId);
    }

    @GetMapping("/{id}/recommendations")
    public Collection<Film> findRecommendations(@PathVariable String id){
        log.info("Получен запрос GET к эндпоинту: /users/{}/recommendations", id);
        return recommendationService.getRecommendations(id);
    }
}
