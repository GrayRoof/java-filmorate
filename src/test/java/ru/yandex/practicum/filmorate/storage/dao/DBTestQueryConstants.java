package ru.yandex.practicum.filmorate.storage.dao;

public final class DBTestQueryConstants {
    public static final String SQL_PREPARE_DB =
            "DELETE FROM LIKES; " +
                    "DELETE FROM GENRELINE; " +
                    "DELETE FROM FRIENDSHIP; " +
                    "DELETE FROM DIRECTORLINE; " +
                    "DELETE FROM USEFUL; " +
                    "DELETE FROM DIRECTORS; " +
                    "DELETE FROM EVENTS; " +
                    "DELETE FROM REVIEWS; " +
                    "DELETE FROM USERS; " +
                    "DELETE FROM FILM; " +
                    "ALTER TABLE USERS ALTER COLUMN USERID RESTART WITH 1; " +
                    "ALTER TABLE DIRECTORS ALTER COLUMN DIRECTORID RESTART WITH 1; " +
                    "ALTER TABLE FILM ALTER COLUMN FILMID RESTART WITH 1; " +
                    "ALTER TABLE EVENTS ALTER COLUMN EVENTID RESTART WITH 1; " +
                    "ALTER TABLE REVIEWS ALTER COLUMN REVIEWID RESTART WITH 1;";
}
