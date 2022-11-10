package ru.yandex.practicum.filmorate.storage.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DBGenreStorage {

    private final Logger log = LoggerFactory.getLogger(DBGenreStorage.class);
    private final JdbcTemplate jdbcTemplate;

    public DBGenreStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean deleteFilmGenres(int filmId) {
        String deleteOldGenres = "delete from GENRELINE where FILMID = ?";
        jdbcTemplate.update(deleteOldGenres, filmId);
        return true;
    }

    public boolean addFilmGenres(int filmId, Collection<Genre> genres) {
        for (Genre genre : genres) {
            String setNewGenres = "insert into GENRELINE (FILMID, GENREID) values (?, ?) ON CONFLICT DO NOTHING";
            jdbcTemplate.update(setNewGenres, filmId, genre.getId());
        }
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
        }
        catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр с идентификатором " +
                    genreId + " не зарегистрирован!");
        }
        return genre;
    }

    private Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre(rs.getInt("GenreID"), rs.getString("Name"));
        return genre;
    }
}
