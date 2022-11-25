package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FilmStorage {
    boolean containsFilm(int filmId);

    Film getFilm(int filmId);
    Collection<Film> getAllFilms();
    Film addFilm(Film film);
    Film updateFilm(Film film);

    boolean deleteFilm(int film);

    boolean addLike(int filmId, int userId);
    boolean deleteLike(int filmId, int userId);
    Collection<Film> getMostPopularFilms(int count);
    Map<Integer, BitSet> getRelatedLikesByUserId(int userId);
    BitSet getLikesOfUserList(List<Integer> usersId);
    Collection<Film> getFilmsOfIdArray(String idString);
    Collection<Film> getSortedFilmWithDirector(Integer id, String sortBy);
    Collection<Film> getCommonFilms(int userId, int otherUserId);

}
