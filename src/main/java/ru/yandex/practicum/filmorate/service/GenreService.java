package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

@Service
public class GenreService {

    private final GenreStorage genreStorage;

    @Autowired
    public GenreService(@Qualifier(UsedStorageConsts.QUALIFIER) GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Collection<Genre> getAll() {
        return genreStorage.getAll();
    }
    public Collection<Genre> getFilmGenres(int filmId) {
        return genreStorage.getByFilmId(filmId);
    }

    public Genre get(String supposedId) {
        int genreId = intFromString(supposedId);
        return genreStorage.getById(genreId);
    }

    public boolean deleteFilmGenres(int filmId) {
        return genreStorage.deleteFilmGenres(filmId);
    }

    public void load(Collection<Film> films) {
        genreStorage.load(films);
    }

    private Integer intFromString(final String supposedInt) {
        try {
            return Integer.valueOf(supposedInt);
        } catch (NumberFormatException exception) {
            return Integer.MIN_VALUE;
        }
    }
}
