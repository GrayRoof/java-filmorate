package ru.yandex.practicum.filmorate.storage.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.event.OnDeleteUserEvent;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Component("DBFilmStorage")
public class DBFilmStorage implements FilmStorage {

    private final Logger log = LoggerFactory.getLogger(DBFilmStorage.class);
    private final JdbcTemplate jdbcTemplate;
    private final GenreService genreService;

    public DBFilmStorage(JdbcTemplate jdbcTemplate, GenreService genreService) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreService = genreService;
    }

    @Override
    public boolean containsFilm(int filmId) {
        SqlRowSet result = jdbcTemplate.queryForRowSet("select FILMID from FILM where FILMID = ?;", filmId);
        return result.next();
    }

    @Override
    public Film getFilm(int filmId) {

        String sqlFilm = "select * from FILM " +
                "INNER JOIN MPA R on FILM.RATINGID = R.RATINGID " +
                "where FILMID = ?";
        Film film;
        try {
            film = jdbcTemplate.queryForObject(sqlFilm, (rs, rowNum) -> makeFilm(rs), filmId);
        }
        catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с идентификатором " +
                    filmId + " не зарегистрирован!");
        }
        log.info("Найден фильм: {} {}", film.getId(), film.getName());
        return film;
    }

    @Override
    public Collection<Film> getAllFilms() {
        String sql = "select * from FILM " +
                "INNER JOIN MPA R on FILM.RATINGID = R.RATINGID ";
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

        if (!film.getGenres().isEmpty()) {
            genreService.addFilmGenres(id, film.getGenres());
        }
        if (film.getLikes() != null) {
            for (Integer userId : film.getLikes()) {
                addLike(film.getId(), userId);
            }
        }
        updateLikeRating(id);
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

        genreService.deleteFilmGenres(film.getId());
        if (!film.getGenres().isEmpty()) {
            genreService.addFilmGenres(film.getId(), film.getGenres());
        }

        if(film.getLikes() != null) {
            for (Integer userId : film.getLikes()) {
                addLike(film.getId(), userId);
            }
        }
        updateLikeRating(film.getId());
        return getFilm(film.getId());
    }

    @Override
    public boolean deleteFilm(int filmId) {
        String sqlQuery = "delete from FILM where FILMID = ?";
        return jdbcTemplate.update(sqlQuery, filmId) > 0;
    }

    @Override
    public boolean addLike(int filmId, int userId) {
        String sql = "select * from LIKES where USERID = ? and FILMID = ?";
        SqlRowSet existLike = jdbcTemplate.queryForRowSet(sql, userId, filmId);
        if (!existLike.next()) {
            String setLike = "insert into LIKES (USERID, FILMID) values (?, ?) ";
            jdbcTemplate.update(setLike, userId, filmId);
        }
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, userId, filmId);
        log.info(String.valueOf(rs.next()));
        updateLikeRating(filmId);
        return rs.next();
    }

    @Override
    public boolean deleteLike(int filmId, int userId) {
        String deleteLike = "delete from LIKES where FILMID = ? and USERID = ?";
        jdbcTemplate.update(deleteLike, filmId, userId);
        updateLikeRating(filmId);
        return true;
    }

    @Override
    public Collection<Film> getMostPopularFilms(int count) {
        String sqlCacheMostPopular = "select FILM.FILMID" +
                ",FILM.NAME ,FILM.DESCRIPTION ,RELEASEDATE ,DURATION ,RATE " +
                ",R.RATINGID, R.NAME, R.DESCRIPTION from FILM " +
                "inner join MPA R on R.RATINGID = FILM.RATINGID " +
                "group by FILM.FILMID " +
                "ORDER BY RATE desc " +
                "limit ?";
        Collection<Film> films = jdbcTemplate.query(sqlCacheMostPopular, (rs, rowNum) -> makeFilm(rs), count);
        return films;
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        int filmId = rs.getInt("FilmID");
        Film film = new Film(
                filmId,
                rs.getString("Name"),
                rs.getString("Description"),
                Objects.requireNonNull(rs.getDate("ReleaseDate")).toLocalDate(),
                rs.getLong("Duration"),
                rs.getInt("Rate"),
                new Mpa(rs.getInt("MPA.RatingID"),
                        rs.getString("MPA.Name"),
                        rs.getString("MPA.Description")),
                (List<Genre>) genreService.getFilmGenres(filmId),
                getFilmLikes(filmId));
        return film;
    }

    private List<Integer> getFilmLikes(int filmId) {
        String sqlGetLikes = "select USERID from LIKES where FILMID = ?";
        List<Integer> likes = jdbcTemplate.queryForList(sqlGetLikes, Integer.class, filmId);
        return likes;
    }

    private boolean updateLikeRating(int filmId) {
        String sqlUpdateRate = "update FILM set RATE = ( select count(USERID) from LIKES where FILMID = ?) where FILMID = ?";
        int response = jdbcTemplate.update(sqlUpdateRate, filmId, filmId);
        log.info(String.valueOf(response));
        return true;
    }

    @EventListener
    private void handleOnDeleteUser(OnDeleteUserEvent event) {
        String sqlUpdateAllRates =
                "update FILM set RATE = ( select count(USERID) from LIKES where LIKES.FILMID = FILM.FILMID );";
        jdbcTemplate.update(sqlUpdateAllRates);
    }
}
