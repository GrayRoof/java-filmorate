package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.UserDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Set;

@Slf4j
@Service
@Qualifier("users")
public class UserService {
    private int increment = 0;
    @Autowired
    private Validator validator;

    @Autowired
    private UserDao userDao;

    private void validate(User user) {
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
        return userDao.getAllUsers();
    }

    public User add(User user) {
        validate(user);
        return userDao.addUser(user);
    }

    public User update(User user) {
        validate(user);
        if(!userDao.getAllUsers().contains(user)) {
            throw new NotFoundException("Пользователь с идентификатором " +
                    user.getId() + " не зарегистрирован!");
        }
        return userDao.addUser(user);
    }
}
