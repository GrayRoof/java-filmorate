package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.exception.WrongIdException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    private int increment = 0;
    private final Validator validator;

    private final UserStorage userStorage;

    @Autowired
    public UserService(Validator validator, UserStorage userStorage) {
        this.validator = validator;
        this.userStorage = userStorage;
    }

    /**
     * Возвращает коллекцию пользователей
     * */
    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    /**
     * Добавляет пользователя в коллекцию
     * Возвращает добавленного пользователя
     * @exception UserValidationException в случае, если пользователь содержит недопустимое содержание полей
     * */
    public User add(final User user) {
        validate(user);
        return userStorage.addUser(user);
    }

    /**
     * Обновляет пользователя в коллекции
     * Возвращает обновленного пользователя
     * @exception UserValidationException в случае, если пользователь содержит недопустимое содержание полей
     * */
    public User update(final User user) {
        validate(user);
        return userStorage.updateUser(user);
    }

    /**
     * Добавляет пользователя в друзья другому пользователю
     * @param supposedUserId - идентификатор пользователя
     * @param supposedFriendId - идентификатор друга
     * */
    public void addFriend(final String supposedUserId, final String supposedFriendId) {
        User user = getStoredUser(supposedUserId);
        User friend = getStoredUser(supposedFriendId);
        user.addFriend(friend.getId());
        friend.addFriend(user.getId());
    }

    /**
     * Удаляет пользователя из друзей другого пользователя
     * @param supposedUserId - идентификатор пользователя
     * @param supposedFriendId - идентификатор друга
     * */
    public void deleteFriend(final String supposedUserId, final  String supposedFriendId) {
        User user = getStoredUser(supposedUserId);
        User friend = getStoredUser(supposedFriendId);
        user.deleteFriend(friend.getId());
        friend.deleteFriend(user.getId());
    }

    /**
     * Возвращает коллекцию пользователей, которые являются друзьями для заданного пользователя
     * @param supposedUserId - идентификатор пользователя
     * */
    public Collection<User> getFriends(final String supposedUserId) {
        User user = getStoredUser(supposedUserId);
        Collection<User> friends = new HashSet<>();
        for (Integer id : user.getFriends()) {
            friends.add(userStorage.getUser(id));
        }
        return friends;
    }

    /**
     * Возвращает коллекцию пользователей, которые являются общими друзьями двух заданных пользователей
     * @param supposedUserId - идентификатор пользователя
     * @param supposedOtherId - идентификатор другого пользователя
     * */
    public Collection<User> getCommonFriends(final String supposedUserId, final String supposedOtherId) {
        User user = getStoredUser(supposedUserId);
        User otherUser = getStoredUser(supposedOtherId);
        Collection<User> commonFriends = new HashSet<>();
        for (Integer id : user.getFriends()) {
            if (otherUser.getFriends().contains(id)) {
                commonFriends.add(userStorage.getUser(id));
            }
        }
        return commonFriends;
    }

    /**
     * Возвращает пользователя по идентификатора
     * @param supposedId - идентификатор пользователя
     * */
    public User getUser(final String supposedId) {
        return getStoredUser(supposedId);
    }

    private void validate(final User user) {
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
        if (user.getId() == 0) {
            user.setId(++increment);
        }
    }

    private Integer idFromString(final String supposedId) {
        try {
            return Integer.valueOf(supposedId);
        } catch (NumberFormatException exception) {
            return Integer.MIN_VALUE;
        }
    }

    private User getStoredUser(final String supposedId) {
        final int userId = idFromString(supposedId);
        if (userId == Integer.MIN_VALUE) {
            throw new WrongIdException("Не удалось распознать идентификатор пользователя: " +
                    "значение " + supposedId);
        }
        User user = userStorage.getUser(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с идентификатором " +
                    userId + " не зарегистрирован!");
        }
        return user;
    }
}
