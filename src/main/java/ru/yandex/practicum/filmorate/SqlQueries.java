package ru.yandex.practicum.filmorate;

public class SqlQueries {

    public static final String FILMS_SELECTED_BY_YEAR = "select film.* " +
            "FROM FILM " +
            "WHERE YEAR(film.RELEASEDATE) = ? " +
            "ORDER BY FILM.RATE DESC " +
            "LIMIT ?";

    public static final String FILMS_SELECTED_BY_GENRE = "select film.* " +
            "FROM FILM " +
            "JOIN GENRELINE gl ON gl.FILMID = film.FILMID AND gl.GENREID = ? " +
            "ORDER BY FILM.RATE DESC " +
            "LIMIT ?";

    public static final String FILMS_SELECTED_BY_YEAR_GENRE = "select film.* " +
            "FROM FILM " +
            "JOIN GENRELINE gl ON gl.FILMID = film.FILMID AND gl.GENREID = ? AND YEAR(film.RELEASEDATE) = ? " +
            "ORDER BY FILM.RATE DESC " +
            "LIMIT ?";
}
