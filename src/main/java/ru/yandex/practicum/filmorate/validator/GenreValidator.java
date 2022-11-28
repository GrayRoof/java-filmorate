package ru.yandex.practicum.filmorate.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@Component
public class GenreValidator {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreValidator(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public void validateGenreById(Integer genreId){
        String sqlQuery = "select count(*) from genre where genreid = ?;";
        Integer id = jdbcTemplate.queryForObject(sqlQuery, Integer.class, genreId);
        if (id == null || id != 1) throw new NotFoundException("Жанра с ID "+ genreId +" не существует");
    }
}