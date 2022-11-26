package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Director;

import javax.validation.ValidationException;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.storage.dao.DBTestQueryConstants.SQL_PREPARE_DB;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DirectorServiceTest {
    @Autowired
    DirectorService service;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(SQL_PREPARE_DB);
    }

    @Test
    void shouldAddDirectorWithNormalName() {
        Director director = new Director(10, "Test name");
        Director addedUser = service.addDirector(director);
        assertNotEquals(10, addedUser.getId());
        assertEquals(1, service.getAllDirectors().size());
    }

    @Test
    void shouldAddDirectorWithBadName() {
        Director director = new Director(10, " ");
        assertThrows(ValidationException.class, () -> service.addDirector(director));
    }

    @Test
    void shouldAddDirectorWithoutName() {
        Director director = new Director();
        assertThrows(ValidationException.class, () -> service.addDirector(director));
    }

    @Test
    void shouldUpdateDirectorWithNormalName() {
        Director director = new Director(10, "Test name");
        Director addedUser = service.addDirector(director);
        Director director2 = new Director(addedUser.getId(), "Updated Dir");
        Director updatedDirector = service.updateDirector(director2);
        assertEquals(1, service.getAllDirectors().size());
        assertEquals("Updated Dir", service.getDirector(1).getName());
    }

    @Test
    void shouldUpdateDirectorWithBadName() {
        Director director = new Director(10, "Test name");
        Director addedUser = service.addDirector(director);
        Director director2 = new Director(addedUser.getId(), " ");
        assertThrows(ValidationException.class, () -> service.updateDirector(director2));
        assertEquals(1, service.getAllDirectors().size());
    }

    @Test
    void shouldUpdateDirectorWithoutName() {
        Director director = new Director(10, "Test name");
        service.addDirector(director);
        Director director2 = new Director();
        assertThrows(ValidationException.class, () -> service.updateDirector(director2));
        assertEquals(1, service.getAllDirectors().size());
    }

    @Test
    void shouldDeleteDirector() {
        Director director = new Director(10, "Test name");
        Director addedUser = service.addDirector(director);
        service.deleteDirector(addedUser.getId());
        assertEquals(0, service.getAllDirectors().size());
    }
}