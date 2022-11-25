package ru.yandex.practicum.filmorate.storage.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.event.OnDeleteUserEvent;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier(DBStorageConsts.QUALIFIER)
public class DBFilmStorage implements FilmStorage {

    private final Logger log = LoggerFactory.getLogger(DBFilmStorage.class);
    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;

    public DBFilmStorage(
            JdbcTemplate jdbcTemplate,
            @Qualifier(DBStorageConsts.QUALIFIER) GenreStorage genreStorage,
            @Qualifier(DBStorageConsts.QUALIFIER) DirectorStorage directorStorage
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
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
        } catch (EmptyResultDataAccessException e) {
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
        film.setId(id);

        if (!film.getGenres().isEmpty()) {
            mergeFilmGenres(film);
        }
        if (!film.getDirectors().isEmpty()) {
            mergeFilmDirectors(film);
        }
        if (film.getLikes() != null) {
            for (Integer userId : film.getLikes()) {
                addLike(film.getId(), userId);
            }
        }
        updateLikeRating(id);

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        isExist(film.getId());
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
        genreStorage.deleteFilmGenres(film.getId());
        if (!film.getGenres().isEmpty()) {
            mergeFilmGenres(film);
        }
        directorStorage.deleteFilmDirector(film.getId());
        if (!film.getDirectors().isEmpty()) {
            mergeFilmDirectors(film);
        }
        if (film.getLikes() != null) {
            for (Integer userId : film.getLikes()) {
                addLike(film.getId(), userId);
            }
        }
        updateLikeRating(film.getId());
        return film;
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
        String sqlCacheMostPopular = "select FILM.FILMID, FILM.NAME, FILM.DESCRIPTION, " +
                "RELEASEDATE, DURATION, RATE, R.RATINGID, R.NAME, R.DESCRIPTION from FILM " +
                "inner join MPA R on R.RATINGID = FILM.RATINGID " +
                "group by FILM.FILMID " +
                "ORDER BY RATE desc " +
                "limit ?";
        return jdbcTemplate.query(sqlCacheMostPopular, (rs, rowNum) -> makeFilm(rs), count);
    }

    @Override
    public Collection<Film> getMostPopularByGenre(int count, int genreId){
        String sqlCacheMostPopular = "select * " +
                "from FILM F " +
                "inner join MPA R on R.RATINGID = F.RATINGID " +
                "JOIN GENRELINE GL ON GL.FILMID=F.FILMID " +
                "WHERE GL.GENREID = ? " +
                "ORDER BY RATE desc " +
                "limit ?";
        return jdbcTemplate.query(sqlCacheMostPopular, (rs, rowNum) -> makeFilm(rs), genreId, count);
    }

    @Override
    public Collection<Film> getMostPopularByYear(int year, int count){
        String sqlQuery = "select * " +
                "FROM FILM " +
                "JOIN MPA R on FILM.RATINGID = R.RATINGID " +
                "WHERE YEAR(RELEASEDATE) = ? " +
                "ORDER BY RATE DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), year, count);
    }

    @Override
    public Collection<Film> getSortedByGenreAndYear(int genreId, int year, int count){
        String sqlQuery = "select * " +
                "FROM FILM " +
                "JOIN MPA R on FILM.RATINGID = R.RATINGID " +
                "JOIN GENRELINE gl ON gl.FILMID = FILM.FILMID " +
                "WHERE gl.GENREID = ? AND YEAR(RELEASEDATE) = ? " +
                "GROUP BY FILM.FILMID " +
                "ORDER BY RATE DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), genreId, year, count);
    }

    @Override
    public Collection<Film> getSortedFilmWithDirector(Integer id, String sortBy) {
        String sort;
        switch (sortBy) {
            case "year":
                sort = "f.RELEASEDATE";
                break;
            case "likes":
                sort = "count(DISTINCT L.USERID) DESC";
                break;
            default:
                sort = "f.FILMID";
                break;
        }
        String sql = "SELECT *" +
                " FROM film as f" +
                " INNER JOIN MPA R on R.RATINGID = f.RATINGID" +
                " LEFT OUTER JOIN DIRECTORLINE D on f.FILMID = D.FILMID" +
                " LEFT OUTER JOIN LIKES L on f.FILMID = L.FILMID" +
                " WHERE DIRECTORID = ?" +
                " group by f.FILMID" +
                " ORDER BY " + sort;
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), id);
    }

    @Override
    public Map<Integer, BitSet> getRelatedLikesByUserId(int userId) {
        Map<Integer, BitSet> likes = new HashMap<>();
        String sql = "select L2.FILMID, L2.USERID from LIKES L " +
                "inner join LIKES L2 on L.FILMID = L2.FILMID " +
                "where L.USERID = ?";
        SqlRowSet existLikes = jdbcTemplate.queryForRowSet(sql, userId);
        while (existLikes.next()) {
            int currentKey = existLikes.getInt("userId");
            if (!likes.containsKey(currentKey)) { likes.put(currentKey, new BitSet()); }
            likes.get(currentKey).set(existLikes.getInt("filmId"));
        }
        return likes;
    }

    @Override
    public BitSet getLikesOfUserList(List<Integer> usersId) {
        BitSet likes = new BitSet();
        String sql = "select distinct FILMID from LIKES where USERID in (" +
                usersId.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
        SqlRowSet existLikes = jdbcTemplate.queryForRowSet(sql);
        while (existLikes.next()) {
            likes.set(existLikes.getInt("filmId"));
        }
        return likes;
    }

    @Override
    public Collection<Film> getFilmsOfIdArray(String idString) {
        String sql = "select * from FILM " +
                "inner join MPA M on FILM.RATINGID = M.RATINGID " +
                "where FILM.FILMID in (" + idString + ")";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public Collection<Film> getCommonFilms(int userId, int otherUserId) {
        String sqlGetCommon =
                "with COMMON (COMMONID) as " +
                        "( " +
                        "   select distinct FILMID from LIKES where USERID = ? " +
                        "   intersect " +
                        "   select distinct FILMID from LIKES where USERID = ? " +
                        ") " +
                        "select FILM.FILMID, FILM.NAME, FILM.DESCRIPTION, FILM.RELEASEDATE, FILM.DURATION, FILM.RATE, " +
                        "R.RATINGID, R.NAME, R.DESCRIPTION " +
                        "from FILM " +
                        "inner join MPA R on R.RATINGID = FILM.RATINGID " +
                        "where FILMID in (select COMMONID from COMMON) " +
                        "group by FILM.FILMID " +
                        "order by RATE desc;";
        return jdbcTemplate.query(sqlGetCommon, (rs, rowNum) -> makeFilm(rs), userId, otherUserId);
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        int filmId = rs.getInt("FilmID");
        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        genreStorage.getGenresByFilmId(filmId).forEach(genre -> genres.add(genre));
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
                genres,
                new LinkedHashSet<>(),
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

    private boolean isExist(Integer id) {
        if (getFilm(id) == null) {
            throw new NotFoundException(String.format("Film with id=%d not found.", id));
        }
        return true;
    }

    private void mergeFilmGenres(Film film) {
        final List<Genre> genreList = new ArrayList<>(film.getGenres());
        jdbcTemplate.batchUpdate("MERGE INTO GENRELINE (FILMID, GENREID) values (?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, film.getId());
                ps.setLong(2, genreList.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return film.getGenres().size();
            }
        });
    }

    private void mergeFilmDirectors(Film film) {
        final List<Director> directorsList = new ArrayList<>(film.getDirectors());
        jdbcTemplate.batchUpdate("MERGE INTO DIRECTORLINE (FILMID, DIRECTORID) values (?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, film.getId());
                ps.setLong(2, directorsList.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return film.getDirectors().size();
            }
        });
    }

    @EventListener
    public void handleOnDeleteUser(OnDeleteUserEvent event) {
        String sqlUpdateAllRates =
                "update FILM set RATE = ( select count(USERID) from LIKES where LIKES.FILMID = FILM.FILMID );";
        jdbcTemplate.update(sqlUpdateAllRates);
    }


}
