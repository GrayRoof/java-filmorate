package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;


import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private int increment = 0;
    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос GET к эндпоинту: /films");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получен запрос POST. Данные тела запроса: {}", film);
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new FilmValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        film.setId(++increment);
        films.put(film.getId(), film);
        log.info("Создан объект {} с идентификатором {}", Film.class.getSimpleName(), film.getId());
        return film;
    }

    @PutMapping
    public Film put(@Valid @RequestBody Film film) {
        log.info("Получен запрос PUT. Данные тела запроса: {}", film);
        if(!films.containsValue(film)) {
            throw new NotFoundException("Фильм с идентификатором " +
                    film.getId() + " не зарегистрирован!");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new FilmValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        films.put(film.getId(), film);
        log.info("Обновлен объект {} с идентификатором {}", Film.class.getSimpleName(), film.getId());
        return film;
    }
}
