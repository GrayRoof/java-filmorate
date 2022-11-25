package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface GenreStorage {
    boolean deleteFilmGenres(int filmId);
    Collection<Genre> getGenresByFilmId(int filmId);
    Collection<Genre> getAllGenres();
    Genre getGenreById(int genreId);
    void load(Collection<Film> films);
}
