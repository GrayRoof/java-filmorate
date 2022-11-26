package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.SearchService;

import java.util.Collection;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {


    private final FilmService filmService;
    private final SearchService searchService;

    @Autowired(required = false)
    public FilmController(FilmService filmService, SearchService searchService) {
        this.filmService = filmService;
        this.searchService = searchService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос GET к эндпоинту: /films");
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film findFilm(@PathVariable String id) {
        log.info("Получен запрос GET к эндпоинту: /films/{}", id);
        return filmService.getFilm(id);
    }

    @GetMapping("/popular")
    public Collection<Film> findMostPopular(
            @RequestParam(defaultValue = "10", required = false) String count,
            @RequestParam(defaultValue = "all", required = false) String year,
            @RequestParam(defaultValue = "all", required = false) String genreId) {
        log.info("Получен запрос GET к эндпоинту: /films/popular?count={}", count);
        return filmService.getMostPopularFilms(count, genreId, year);
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

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        log.info("Получен запрос DELETE к эндпоинту: films/{}", id);
        filmService.deleteFilm(id);
        log.info("Удален объект {} с идентификатором {}",
                Film.class.getSimpleName(), id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void putLike(@PathVariable String id, @PathVariable String userId) {
        log.info("Получен запрос PUT к эндпоинту: /films/{}/like/{}", id, userId);
        filmService.addLike(id, userId);
        log.info("Обновлен объект {} с идентификатором {}, добавлен лайк от пользователя {}",
                Film.class.getSimpleName(), id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable String id, @PathVariable String userId) {
        log.info("Получен запрос DELETE к эндпоинту: films/{}/like/{}", id, userId);
        filmService.deleteLike(id, userId);
        log.info("Обновлен объект {} с идентификатором {}, удален лайк от пользователя {}",
                Film.class.getSimpleName(), id, userId);

    }

    @GetMapping("/director/{id}")
    public Collection<Film> getSortedFilmWithDirector(@PathVariable Integer id, @RequestParam String sortBy) {
        log.info("Получен запрос GET к эндпоинту: /films/director/" + id + "?sortBy=" + sortBy);
        return filmService.getSortedFilmWithDirector(id, sortBy);
    }

    @GetMapping("/common")
    public Collection<Film> getCommon(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "friendId") String friendId
    ) {
        log.info("Получен запрос GET к эндпоинту: /films/common, userId={}, friendId={}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping({"/search"})
    public Collection<Film> filmSearch(@RequestParam String query, @RequestParam Set<String> by) {
        log.info("Получен запрос GET к эндпоинту: /films/search?query={}&by={}", query, String.join(",", by));
        return searchService.filmSearch(query, by);
    }
}
