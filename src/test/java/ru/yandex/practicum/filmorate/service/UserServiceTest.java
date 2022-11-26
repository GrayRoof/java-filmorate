package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.storage.dao.DBTestQueryConstants.SQL_PREPARE_DB;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserServiceTest {

    @Autowired
    UserService service;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(SQL_PREPARE_DB);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM friendship;");
        jdbcTemplate.update("DELETE FROM likes;");
        jdbcTemplate.update("DELETE FROM users;");
        jdbcTemplate.update("DELETE FROM film;");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN userid RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE film ALTER COLUMN filmid RESTART WITH 1;");
    }

    @Test
    void shouldAddUserWhenValidUserData() {
        User user = new User(0,
                "correct.email@mail.ru",
                "correctlogin",
                "Correct Name",
                LocalDate.of(2002, 1, 1),
                new ArrayList<>());
        User addedUser = service.add(user);
        assertNotEquals(0, addedUser.getId());
        assertTrue(service.getAll().contains(addedUser));
    }

    @Test
    void shouldSetUserNameWhenEmptyUserName() {
        User user = new User(0,
                "new_correct.email@mail.ru",
                "correctlogin",
                "",
                LocalDate.of(2002, 1, 1),
                new ArrayList<>());
        User addedUser = service.add(user);
        assertNotEquals(0, addedUser.getId());
        assertEquals(addedUser.getLogin(), addedUser.getName());
        assertTrue(service.getAll().contains(addedUser));
    }

    @Test
    void shouldThrowExceptionWhenFailedUserLogin() {
        User user = new User(0,
                "correct.email@mail.ru",
                "incorrect login",
                "Correct Name",
                LocalDate.of(2002, 1, 1),
                new ArrayList<>());
        UserValidationException ex = assertThrows(UserValidationException.class, () -> service.add(user));
        assertEquals("Ошибка валидации Пользователя: " +
                "Логин не может содержать пробелы.", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFailedUserEmail() {
        User user = new User(0,
                "incorrect.email@",
                "correctlogin",
                "Name",
                LocalDate.of(2002, 1, 1),
                new ArrayList<>());
        UserValidationException ex = assertThrows(UserValidationException.class, () -> service.add(user));
        assertEquals("Ошибка валидации Пользователя: " +
                "Введенное значение не является адресом электронной почты.", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFailedUserBirthDate() {
        User user = new User(0,
                "correct.email@mail.ru",
                "correctlogin",
                "Correct Name",
                LocalDate.now().plusDays(1),
                new ArrayList<>());
        UserValidationException ex = assertThrows(UserValidationException.class, () -> service.add(user));
        assertEquals("Ошибка валидации Пользователя: " +
                "Дата рождения не может быть в будущем.", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdateFailedUserId() {
        User user = new User(99,
                "correct.email@mail.ru",
                "correctlogin",
                "Correct Name",
                LocalDate.now().plusYears(-33),
                new ArrayList<>());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.update(user));
        assertEquals("Пользователь с идентификатором 99 не зарегистрирован!", ex.getMessage());
    }
}