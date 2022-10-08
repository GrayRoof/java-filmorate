package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.*;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {


    private final FilmService filmService;

    @Autowired(required = false)
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос GET к эндпоинту: /films");
        return filmService.getFilms();
    }

    //GET /films/popular?count={count}
    @GetMapping("/popular?count={count}")
    public Collection<Film> findMostPopular(@PathVariable Optional<String> count) {
        log.info("Получен запрос GET к эндпоинту: /films/popular?count={count}");
        if(count.isPresent()) {
            return filmService.getMostPopularFilms(count.get());
        } else {
           return filmService.getMostPopularFilms("10");
        }
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Получен запрос POST. Данные тела запроса: {}", film);
        Film validFilm = filmService.add(film);
        log.info("Создан объект {} с идентификатором {}", Film.class.getSimpleName(), validFilm.getId());
        return validFilm;
    }

    @PutMapping
    public Film put(@RequestBody Film film) {
        log.info("Получен запрос PUT. Данные тела запроса: {}", film);
        Film validFilm = filmService.update(film);
        log.info("Обновлен объект {} с идентификатором {}", Film.class.getSimpleName(), validFilm.getId());
        return validFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public void putLike(@PathVariable String id, @PathVariable String userId) {
        log.info("Получен запрос PUT. Данные тела запроса: {}", id);
        filmService.addLike(id, userId);
        log.info("Обновлен объект {} с идентификатором", id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable String id, @PathVariable String userId) {
        log.info("Получен запрос DELETE. Данные тела запроса: {}", id);
        filmService.deleteLike(id, userId);
        log.info("Обновлен объект {} с идентификатором {}", id, userId);

    }
}
