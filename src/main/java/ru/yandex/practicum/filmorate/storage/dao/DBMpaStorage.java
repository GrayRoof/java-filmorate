package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Component
@Qualifier(DBStorageConstants.QUALIFIER)
public class DBMpaStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public DBMpaStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Mpa> getAll() {
        String sqlMpa = "select * from MPA";
        return jdbcTemplate.query(sqlMpa, this::makeMpa);
    }

    @Override
    public Mpa getById(int mpaId) {
        String sqlMpa = "select * from MPA where RATINGID = ?";
        Mpa mpa;
        try {
            mpa = jdbcTemplate.queryForObject(sqlMpa, this::makeMpa, mpaId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Возрастной рейтинг с идентификатором " +
                    mpaId + " не зарегистрирован!");
        }
        return mpa;
    }

    private Mpa makeMpa(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = new Mpa(rs.getInt("RatingID"),
                rs.getString("Name"),
                rs.getString("Description"));
        return mpa;
    }
}
