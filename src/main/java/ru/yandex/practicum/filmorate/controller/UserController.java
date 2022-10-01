package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService service;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос GET к эндпоинту: /users");
        return service.getAllUsers();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Получен запрос POST. Данные тела запроса: {}", user);
        final User validUser = service.add(user);
        log.info("Создан объект {} с идентификатором {}", User.class.getSimpleName(), validUser.getId());
        return validUser;
    }

    @PutMapping
    public User put(@Valid @RequestBody User user) {
        log.info("Получен запрос POST. Данные тела запроса: {}", user);
        final User validUser = service.update(user);
        log.info("Обновлен объект {} с идентификатором {}", User.class.getSimpleName(), validUser.getId());
        return validUser;
    }
}
