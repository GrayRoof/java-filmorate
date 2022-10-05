package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserServiceTest {

    @Autowired
    UserService service;

    @Test
    void shouldAddUserWhenValidUserData() {
        User user = new User();
        user.setLogin("correctlogin");
        user.setName("Correct Name");
        user.setEmail("correct.email@mail.ru");
        user.setBirthday(LocalDate.of(2002, 1, 1));
        User addedUser = service.add(user);
        assertNotEquals(0, addedUser.getId());
        assertTrue(service.getAllUsers().contains(addedUser));
    }

    @Test
    void shouldSetUserNameWhenEmptyUserName() {
        User user = new User();
        user.setLogin("correctlogin");
        user.setName("");
        user.setEmail("correct.email@mail.ru");
        user.setBirthday(LocalDate.of(2002, 1, 1));
        User addedUser = service.add(user);
        assertNotEquals(0, addedUser.getId());
        assertEquals(addedUser.getLogin(), addedUser.getName());
        assertTrue(service.getAllUsers().contains(addedUser));
    }

    @Test
    void shouldThrowExceptionWhenFailedUserLogin() {
        User user = new User();
        user.setLogin("incorrect login");
        user.setName("Correct Name");
        user.setEmail("correct.email@mail.ru");
        user.setBirthday(LocalDate.of(2002, 1, 1));
        UserValidationException ex = assertThrows(UserValidationException.class, () -> service.add(user));
        assertEquals("Ошибка валидации Пользователя: " +
                "Логин не может содержать пробелы.", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFailedUserEmail() {
        User user = new User();
        user.setLogin("correctlogin");
        user.setName("Correct Name");
        user.setEmail("incorrect.email@");
        user.setBirthday(LocalDate.of(2002, 1, 1));
        UserValidationException ex = assertThrows(UserValidationException.class, () -> service.add(user));
        assertEquals("Ошибка валидации Пользователя: " +
                "Введенное значение не является адресом электронной почты.", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFailedUserBirthDate() {
        User user = new User();
        user.setLogin("correctlogin");
        user.setName("Correct Name");
        user.setEmail("correct.email@mail.ru");
        user.setBirthday(LocalDate.now().plusDays(1));
        UserValidationException ex = assertThrows(UserValidationException.class, () -> service.add(user));
        assertEquals("Ошибка валидации Пользователя: " +
                "Дата рождения не может быть в будущем.", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdateFailedUserId() {
        User user = new User();
        user.setId(99);
        user.setLogin("correctlogin");
        user.setName("Correct Name");
        user.setEmail("correct.email@mail.ru");
        user.setBirthday(LocalDate.now().plusYears(-33));
        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.update(user));
        assertEquals("Пользователь с идентификатором 99 не зарегистрирован!", ex.getMessage());
    }
}