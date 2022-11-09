package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.DBGenreStorage;

import java.util.Collection;

@Service
public class GenreService {

    private final DBGenreStorage dbGenreStorage;

    @Autowired
    public GenreService(DBGenreStorage dbGenreStorage) {
        this.dbGenreStorage = dbGenreStorage;
    }

    public Collection<Genre> getAllGenres() {
        return dbGenreStorage.getAllGenres();
    }

    public Collection<Genre> getFilmGenres(int filmId) {
        return dbGenreStorage.getGenresByFilmId(filmId);
    }

    public Genre getGenre(String supposedId) {
        int genreId = intFromString(supposedId);
        return dbGenreStorage.getGenreById(genreId);
    }

    public boolean deleteFilmGenres(int filmId) {
       return dbGenreStorage.deleteFilmGenres(filmId);
    }

    public boolean addFilmGenres(int filmId, Collection<Genre> genres) {
        return dbGenreStorage.addFilmGenres(filmId, genres);
    }

    private Integer intFromString(final String supposedInt) {
        try {
            return Integer.valueOf(supposedInt);
        } catch (NumberFormatException exception) {
            return Integer.MIN_VALUE;
        }
    }

}
