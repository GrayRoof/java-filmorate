package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Component("DBFilmStorage")
public class DBFilmStorage implements FilmStorage{

    private final Logger log = LoggerFactory.getLogger(DBFilmStorage.class);
    private final JdbcTemplate jdbcTemplate;

    public DBFilmStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film getFilm(int filmId) {

        String sqlFilm = "select * from FILM " +
                "INNER JOIN RATINGMPA R on FILM.RATINGID = R.RATINGID " +
                "where FILMID = ?";
        Film film = jdbcTemplate.query(sqlFilm, (rs, rowNum) -> makeFilm(rs), filmId).get(0);

        if(film == null) {
            throw new NotFoundException("Фильм с идентификатором " +
                    filmId + " не зарегистрирован!");
        } else {
            log.info("Найден фильм: {} {}", film.getId(), film.getName());
            return film;

        }
    }

    @Override
    public Collection<Film> getAllFilms() {
        String sql = "select * from FILM " +
                "INNER JOIN RATINGMPA R on FILM.RATINGID = R.RATINGID ";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public Film addFilm(Film film) {
        String sqlQuery = "insert into FILM " +
                "(NAME, DESCRIPTION, RELEASEDATE, DURATION, RATE, RATINGID) " +
                "values (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            ps.setInt(5, film.getRate());
            ps.setInt(6, Math.toIntExact(film.getMpa().getId()));
            return ps;
        }, keyHolder);

        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                String sql = "insert into GENRELINE (FILMID, GENREID) values (?, ?)";
                jdbcTemplate.update(sql,
                        id,
                        genre.getId());
                log.info("Жанры фильма с id = {} обновлены.", film.getId());
            }
        }
        return getFilm(id);
    }

    @Override
    public Film updateFilm(Film film) {
    String sqlQuery = "update FILM " +
            "set NAME = ?, DESCRIPTION = ?, RELEASEDATE = ?, DURATION = ?, RATE = ? ,RATINGID = ? " +
            "where FILMID = ?";
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRate(),
                film.getMpa().getId(),
                film.getId());

        String deleteOldGenres = "delete from GENRELINE where FILMID = ?";
        jdbcTemplate.update(deleteOldGenres, film.getId());
        for (Genre genre : film.getGenres()) {
            String setNewGenres = "insert into GENRELINE (FILMID, GENREID) values (?, ?)";
            jdbcTemplate.update(setNewGenres, film.getId(), genre.getId());
        }

        return getFilm(film.getId());
    }

    @Override
    public boolean deleteFilm(Film film) {
        String sqlQuery = "delete from FILM where FILMID = ?";
        jdbcTemplate.update(sqlQuery, film.getId());
        return true;
    }

    public Collection<Genre> getGenresByFilmId(int filmId) {
        String sqlGenre = "select GENRE.GENREID, NAME from GENRE " +
                "INNER JOIN GENRELINE GL on GENRE.GENREID = GL.GENREID " +
                "where FILMID = ?";
        return jdbcTemplate.query(sqlGenre, (rs, rowNum) -> makeGenre(rs), filmId);
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        Film film = new Film(
                rs.getInt("FilmID"),
                rs.getString("Name"),
                rs.getString("Description"),
                Objects.requireNonNull(rs.getDate("ReleaseDate")).toLocalDate(),
                rs.getLong("Duration"),
                rs.getInt("Rate"),
                new Mpa(rs.getInt("RatingMPA.RatingID"),
                        rs.getString("RatingMPA.Name"),
                        rs.getString("RatingMPA.Description")),
                (List<Genre>) getGenresByFilmId(rs.getInt("FilmID")),
                new ArrayList<>());
        return film;
    }

    private Genre makeGenre(ResultSet rs) throws SQLException {
        Genre genre = new Genre(rs.getInt("GenreID"), rs.getString("Name"));
        return genre;
    }
}
