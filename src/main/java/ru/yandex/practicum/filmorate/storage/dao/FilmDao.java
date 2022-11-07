package ru.yandex.practicum.filmorate.storage.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmDao {
    Collection<Film> findAllFilm();
    Optional<Film> findFilm(int filmId);

    Optional<Film> insertFilm(Film film);

    boolean updateFilm(Film film);

    boolean deleteFilm(int filmId);



}
