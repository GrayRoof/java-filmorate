package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int increment = 0;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос GET к эндпоинту: /users");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Получен запрос POST. Данные тела запроса: {}", user);

        if(user.getName() == null) {
            user.setName(user.getLogin());
            log.info("Поле name не задано. Установлено значение {} из поля login", user.getLogin());
        }else if (user.getName().isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Поле name не содержит буквенных символов. " +
                    "Установлено значение {} из поля login", user.getLogin());
        }
        user.setId(++increment);
        users.put(user.getId(), user);
        log.info("Создан объект {} с идентификатором {}", User.class.getSimpleName(), user.getId());
        return user;
    }

    @PutMapping
    public User put(@Valid @RequestBody User user) {
        log.info("Получен запрос POST. Данные тела запроса: {}", user);
        if(!users.containsValue(user)) {
            throw new NotFoundException("Пользователь с идентификатором " +
                    user.getId() + " не зарегистрирован!");
        }

        if(user.getName() == null) {
            user.setName(user.getLogin());
        } else if (user.getName().isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Обновлен объект {} с идентификатором {}", User.class.getSimpleName(), user.getId());
        return user;
    }
}
