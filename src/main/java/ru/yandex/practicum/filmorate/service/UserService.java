package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Set;

@Service
public class UserService {
    private int increment = 0;
    @Autowired
    private Validator validator;

    @Autowired
    private UserDao userDao;

    public void validate(User user) {
        if (user.getId() == 0) {
            user.setId(++increment);
        }
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            StringBuilder messageBuilder = new StringBuilder();
            for (ConstraintViolation<User> userConstraintViolation : violations) {
                messageBuilder.append(userConstraintViolation.getMessage());
            }
            throw new UserValidationException("Ошибка валидации Фильма: " + messageBuilder, violations);
        }
    }

    public Collection<User> getUsers() {
        return userDao.getAllUsers();
    }

    public User add(User user) {
        return userDao.addUser(user);
    }

    public User update(User user) {
        if(!userDao.getAllUsers().contains(user)) {
            throw new NotFoundException("Фильм с идентификатором " +
                    user.getId() + " не зарегистрирован!");
        }
        return userDao.addUser(user);
    }
}
