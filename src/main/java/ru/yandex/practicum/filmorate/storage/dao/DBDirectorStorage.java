package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;

@Component
@Qualifier(DBStorageConstants.QUALIFIER)
public class DBDirectorStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    public DBDirectorStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Director get(Integer id) {
        String sqlDirector = "select * from DIRECTORS where DIRECTORID = ?";
        Director director;
        try {
            director = jdbcTemplate.queryForObject(sqlDirector, (rs, rowNum) -> makeDirector(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Режиссер с идентификатором " +
                    id + " не зарегистрирован!");
        }
        return director;
    }

    @Override
    public Collection<Director> getAll() {
        String sqlAllDirectors = "select * from DIRECTORS";
        return jdbcTemplate.query(sqlAllDirectors, (rs, rowNum) -> makeDirector(rs));
    }

    @Override
    public Director add(Director director) {
        String sqlQuery = "insert into DIRECTORS " +
                "(NAME) " +
                "values (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        director.setId(id);
        return director;
    }

    @Override
    public Director update(Director director) {
        contains(director.getId());
        String sqlDirector = "update DIRECTORS set " +
                "NAME = ?" +
                "where DIRECTORID = ?";
        jdbcTemplate.update(sqlDirector,
                director.getName(), director.getId());
        return get(director.getId());
    }

    @Override
    public boolean delete(Integer id) {
        contains(id);
        String sqlDirector = "DELETE from DIRECTORS where DIRECTORID = ?";
        jdbcTemplate.update(sqlDirector, id);
        return true;
    }

    @Override
    public boolean deleteFilmDirector(int filmId) {
        String deleteOldDirector = "delete from DIRECTORLINE where FILMID = ?";
        jdbcTemplate.update(deleteOldDirector, filmId);
        return true;
    }

    @Override
    public boolean contains(Integer id) {
        if (get(id) == null) {
            throw new NotFoundException(String.format("Режиссер с id=%d не найден.", id));
        }
        return true;
    }

    @Override
    public void load(Collection<Film> films) {
        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        final Map<Integer, Film> filmById = films.stream().collect(Collectors.toMap(Film::getId, identity()));
        final String sqlQuery = "select * from DIRECTORS d, DIRECTORLINE dl " +
                "where dl.DIRECTORID = d.DIRECTORID AND dl.FILMID IN (" + inSql + ")";

        jdbcTemplate.query(sqlQuery, (rs) -> {
            final Film film = filmById.get(rs.getInt("FILMID"));
            film.getDirectors().add(makeDirector(rs));
        }, films.stream().map(Film::getId).toArray());
    }

    private Director makeDirector(ResultSet rs) throws SQLException {
        int directorId = rs.getInt("DirectorID");
        return new Director(
                directorId,
                rs.getString("Name"));
    }
}
