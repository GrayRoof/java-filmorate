package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmSearchOptions;

import java.util.*;

public interface FilmStorage {
    boolean containsFilm(int filmId);
    Film get(int filmId);
    Collection<Film> getAll();
    Film add(Film film);
    Film update(Film film);
    boolean delete(int film);
    boolean addLike(int filmId, int userId);
    boolean deleteLike(int filmId, int userId);
    Collection<Film> getMostPopular(int count);
    Collection<Film> getMostPopularByGenre(int count, int genreId);
    Collection<Film> getMostPopularByYear(int year, int count);
    Collection<Film> getSortedByGenreAndYear(int genreId, int year, int count);
    Collection<Film> getSortedWithDirector(Integer id, String sortBy);
    Map<Integer, BitSet> getRelatedLikesByUserId(int userId);
    BitSet getLikesOfUserList(List<Integer> usersId);
    Collection<Film> getByIds(List<Integer> ids);
    Collection<Film> getCommon(int userId, int otherUserId);
    Collection<Film> getSortedFromSearch(String query, Set<FilmSearchOptions> params);
}