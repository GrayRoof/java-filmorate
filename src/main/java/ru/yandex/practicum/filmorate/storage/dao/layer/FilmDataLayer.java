package ru.yandex.practicum.filmorate.storage.dao.layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dao.FilmDao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Date;
import java.util.*;

public class FilmDataLayer implements FilmDao {
    private final Logger log = LoggerFactory.getLogger(FilmDataLayer.class);
    private final JdbcTemplate jdbcTemplate;

    public FilmDataLayer(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Film> findAllFilm() {
        String sql = "select * from \"Film\"";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public Optional<Film> findFilm(int filmId) {
        String sqlGenre = "select \"GenreID\", \"Name\" from \"Genre\" " +
                "INNER JOIN \"GenreLine\" GL on \"Genre\".\"GenreID\" = GL.\"GenreID\"" +
                "where \"FilmID\" = ?";
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(sqlGenre, filmId);
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from \"Film\" where \"FilmID\" = ?", filmId);

        Set<Genre> genres = new HashSet<>();
        // обрабатываем результат выполнения запроса
        if(filmRows.next()) {
            Film film = new Film(
                    filmRows.getInt("FilmID"),
                    filmRows.getString("Name"),
                    filmRows.getString("Description"),
                    Objects.requireNonNull(filmRows.getDate("ReleaseDate")).toLocalDate(),
                    filmRows.getLong("Duration"),
                    new Mpa(),
                    genres,
                    new HashSet<>());

            log.info("Найден фильм: {} {}", film.getId(), film.getName());

            return Optional.of(film);
        } else {
            log.info("Фильм с идентификатором {} не найден.", filmId);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Film> insertFilm(Film film) {
        String sqlQuery = "insert into \"Film\" (\"Name\", \"Description\", \"ReleaseDate\", \"Duration\", \"RatingID\") values (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setDate(2, Date.valueOf(film.getReleaseDate()));
            ps.setString(3, film.getDescription());
            ps.setLong(4, film.getDuration());
           // ps.setInt(5, film.getRate());
            ps.setInt(6, Math.toIntExact(film.getMpa().getId()));
            return ps;
        }, keyHolder);

        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                String sql = "insert into \"Genre\" values (?, ?)";
                jdbcTemplate.update(sql,
                        id,
                        genre.getId());
                log.info("Жанры фильма с id = {} обновлены.", film.getId());
            }
        }
        return findFilm(id);
    }

    @Override
    public boolean updateFilm(Film film) {
        return false;
    }

    @Override
    public boolean deleteFilm(int filmId) {
        return false;
    }

    private Film makeFilm(ResultSet rs) {
        return null;
    }
}
