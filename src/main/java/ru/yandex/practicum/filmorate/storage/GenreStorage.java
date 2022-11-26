package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface GenreStorage {
    boolean deleteFilmGenres(int filmId);
    Collection<Genre> getByFilmId(int filmId);
    Collection<Genre> getAll();
    Genre getById(int genreId);
    void load(Collection<Film> films);
}
