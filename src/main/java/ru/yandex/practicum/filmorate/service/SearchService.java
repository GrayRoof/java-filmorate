package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validator.SearchValidator;

import java.util.Collection;
import java.util.Set;

@Service
public class SearchService {
    private final SearchValidator searchValidator;
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    public SearchService(
            SearchValidator searchValidator,
            @Qualifier(UsedStorageConsts.QUALIFIER) FilmStorage filmStorage, FilmService filmService) {
        this.searchValidator = searchValidator;
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    /**
     * Возвращает коллекцию фильмов, отсортированную по убыванию рейтинга
     * при поисковом запросе с параметрами
     * @param query - подстрока, которую необходимо найти (без учёта регистра)
     * @param by - ключи, задающие таблицы, в которых необходимо производить поиск
     * */
    public Collection<Film> filmSearch(String query, Set<String> by) {
        searchValidator.validateQuery(query);
        searchValidator.validateBy(by);
        Collection<Film> films = filmStorage.getSortedFilmFromSearch(query, by);
        if(!films.isEmpty()) filmService.addExtraFilmData(films);
        return films;
    }

}
