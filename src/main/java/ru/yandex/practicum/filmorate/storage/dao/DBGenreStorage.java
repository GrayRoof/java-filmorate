package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;

@Component
public class DBGenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public DBGenreStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean deleteFilmGenres(int filmId) {
        String deleteOldGenres = "delete from GENRELINE where FILMID = ?";
        jdbcTemplate.update(deleteOldGenres, filmId);
        return true;
    }

    public Collection<Genre> getGenresByFilmId(int filmId) {
        String sqlGenre = "select GENRE.GENREID, NAME from GENRE " +
                "INNER JOIN GENRELINE GL on GENRE.GENREID = GL.GENREID " +
                "where FILMID = ?";
        return jdbcTemplate.query(sqlGenre, this::makeGenre, filmId);
    }

    public Collection<Genre> getAllGenres() {
        String sqlGenre = "select GENREID, NAME from GENRE ORDER BY GENREID";
        return jdbcTemplate.query(sqlGenre, this::makeGenre);
    }

    public Genre getGenreById(int genreId) {
        String sqlGenre = "select * from GENRE where GENREID = ?";
        Genre genre;
        try {
            genre = jdbcTemplate.queryForObject(sqlGenre, this::makeGenre, genreId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр с идентификатором " +
                    genreId + " не зарегистрирован!");
        }
        return genre;
    }

    private Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre(rs.getInt("GenreID"), rs.getString("Name"));
        return genre;
    }

    public void load(Collection<Film> films) {
        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        final Map<Integer, Film> filmById = films.stream().collect(Collectors.toMap(Film::getId, identity()));
        final String sqlQuery = "select * from GENRE g, GENRELINE gl " +
                "where gl.GENREID = g.GENREID AND gl.FILMID IN (" + inSql + ")";

        jdbcTemplate.query(sqlQuery, (rs) -> {
            final Film film = filmById.get(rs.getInt("FILMID"));
            film.getGenres().add(makeGenre(rs, 0));
        }, films.stream().map(Film::getId).toArray());
    }
}
