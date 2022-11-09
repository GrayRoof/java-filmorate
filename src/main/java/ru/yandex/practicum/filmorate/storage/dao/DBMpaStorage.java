package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Component
public class DBMpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public DBMpaStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Collection<Mpa> getAllMpa() {
        String sqlMpa = "select * from RATINGMPA";
        return jdbcTemplate.query(sqlMpa, this::makeMpa);
    }

    public Mpa getMpaById(int mpaId) {
        String sqlMpa = "select * from RATINGMPA where RATINGID = ?";
        return jdbcTemplate.queryForObject(sqlMpa, this::makeMpa, mpaId);
    }

    private Mpa makeMpa(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = new Mpa(rs.getInt("RatingID"),
                rs.getString("Name"),
                rs.getString("Description"));
        return mpa;
    }

}
