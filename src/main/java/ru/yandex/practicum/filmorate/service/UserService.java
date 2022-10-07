package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Set;

@Slf4j
@Service
@Qualifier("users")
public class UserService {
    private int increment = 0;
    private final Validator validator;

    private final UserStorage userStorage;

    @Autowired
    public UserService(Validator validator, UserStorage userStorage) {
        this.validator = validator;
        this.userStorage = userStorage;
    }

    private void validate(final User user) {
        if (user.getId() == 0) {
            user.setId(++increment);
        }
        if(user.getName() == null) {
            user.setName(user.getLogin());
            log.info("UserService: Поле name не задано. Установлено значение {} из поля login", user.getLogin());
        }else if (user.getName().isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("UserService: Поле name не содержит буквенных символов. " +
                    "Установлено значение {} из поля login", user.getLogin());
        }
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            StringBuilder messageBuilder = new StringBuilder();
            for (ConstraintViolation<User> userConstraintViolation : violations) {
                messageBuilder.append(userConstraintViolation.getMessage());
            }
            throw new UserValidationException("Ошибка валидации Пользователя: " + messageBuilder, violations);
        }
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User add(final User user) {
        validate(user);
        return userStorage.addUser(user);
    }

    public User update(final User user) {
        validate(user);
        if(!userStorage.getAllUsers().contains(user)) {
            throw new NotFoundException("Пользователь с идентификатором " +
                    user.getId() + " не зарегистрирован!");
        }
        return userStorage.addUser(user);
    }

    public void addFriend(final Integer userId, final Integer friendId) {

    }

    public void deleteFriend(final Integer userId, final  Integer friendId) {

    }

    public Collection<User> getFriends(final Integer userId) {
        return null;
    }

    public Collection<User> getCommonFriends(final Integer userId, final Integer otherId) {
        return null;
    }

}
