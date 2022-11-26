package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface DirectorStorage {
    Director get(Integer id);
    Collection<Director> getAll();
    Director add(Director director);
    Director update(Director director);
    boolean delete(Integer id);
    boolean deleteFilmDirector(int filmId);
    boolean contains(Integer id);
    void load(Collection<Film> films);
}
