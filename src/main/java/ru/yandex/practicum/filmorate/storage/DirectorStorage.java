package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface DirectorStorage {
    Director getDirector(Integer id);
    Collection<Director> getAllDirectors();
    Director addDirector(Director director);
    Director updateDirector(Director director);
    boolean deleteDirector(Integer id);
    boolean deleteFilmDirector(int filmId);
    boolean isExist(Integer id);
    void load(Collection<Film> films);
}
