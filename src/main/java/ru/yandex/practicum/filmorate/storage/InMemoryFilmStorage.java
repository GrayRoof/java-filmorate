package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public boolean containsFilm(int filmId) {
        return films.containsKey(filmId);
    }

    @Override
    public Film getFilm(int filmId) {
        return films.get(filmId);
    }

    @Override
    public Collection<Film> getAllFilms() {
        Collection<Film> allFilms = films.values();
        if (allFilms.isEmpty()) {
            allFilms.addAll(films.values());
        }
        return allFilms;
    }

    @Override
    public Film addFilm(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!getAllFilms().contains(film)) {
            throw new NotFoundException("Фильм с идентификатором " +
                    film.getId() + " не зарегистрирован!");
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public boolean deleteFilm(int filmId) {
        //TODO(?): cascade deletion, real retval
        films.remove(filmId);
        return true;
    }

    @Override
    public boolean addLike(int filmId, int userId) {
        Film film = films.get(filmId);
        film.addLike(userId);
        updateFilm(film);
        return true;
    }

    @Override
    public boolean deleteLike(int filmId, int userId) {
        Film film = films.get(filmId);
        film.deleteLike(userId);
        updateFilm(film);
        return true;
    }

    @Override
    public Collection<Film> getMostPopularFilms(int size) {
        Collection<Film> mostPopularFilms = getAllFilms().stream()
                .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
                .limit(size)
                .collect(Collectors.toCollection(HashSet::new));
        return mostPopularFilms;
    }

    @Override
    public Collection<Film> getMostPopularByGenre(int count, int genreId) {
        return null;
    }

    @Override
    public Collection<Film> getMostPopularByYear(int year, int count) {
        return null;
    }

    @Override
    public Collection<Film> getSortedByGenreAndYear(int genreId, int year, int count) {
        return null;
    }

    @Override
    public Collection<Film> getSortedFilmWithDirector(Integer id, String sortBy) {
       throw new UnsupportedOperationException();
       }

    @Override
    public Map<Integer, BitSet> getRelatedLikesByUserId(int userId) {
        return null;
    }

    @Override
    public BitSet getLikesOfUserList(List<Integer> usersId) {
        return null;
    }

    @Override
    public Collection<Film> getFilmsOfIdArray(String idString) {
        return null;
    }

    @Override
    public Collection<Film> getCommonFilms(int userId, int otherUserId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Film> getSortedWithYear(int year, int count) {
        return null;
    }

}
