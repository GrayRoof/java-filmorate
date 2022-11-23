package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

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

    Collection<Film> getMostPopularFilms(String count, String genreId, String year);

    Collection<Film> getSortedFilmWithDirector(Integer id, String sortBy);
    Collection<Film> getCommonFilms(int userId, int otherUserId);

    Collection<Film> getSortedWithYear(int year, int count);

    Collection<Film> getMostPopularByGenre(String parseInt, String parseInt1);

    Collection<Film> getMostPopularByYear(int parseInt, int parseInt1);

    Collection<Film> getSortedByGenreAndYear(String genreId, String year, String count);
}
