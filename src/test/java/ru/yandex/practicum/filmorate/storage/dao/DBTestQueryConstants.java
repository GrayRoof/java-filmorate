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

    public static final String SQL_FILL_DB =
            "INSERT INTO users (email, login, name, birthday)\n" +
                    "VALUES ('1@1.com', 'user1', 'name1', '2001-01-01'),\n" +
                    "       ('2@2.com', 'user2', 'name2', '2002-02-02'),\n" +
                    "       ('3@3.com', 'user3', 'name3', '2003-03-03'),\n" +
                    "       ('4@4.com', 'user4', 'name4', '2004-04-04'),\n" +
                    "       ('5@5.com', 'user5', 'name5', '2001-01-05'),\n" +
                    "       ('6@6.com', 'user6', 'name6', '2000-01-10'),\n" +
                    "       ('7@7.com', 'user7', 'name7', '2001-07-17'),\n" +
                    "       ('8@8.com', 'user8', 'name8', '2001-07-17'),\n" +
                    "       ('9@9.com', 'user9', 'name9', '2001-07-17'),\n" +
                    "       ('10@10.com', 'user10', 'name10', '2001-07-17');\n" +
                    "INSERT INTO directors (name)\n" +
                    "VALUES ('Ivanov'),\n" +
                    "       ('Svetlov'),\n" +
                    "       ('Svetlanov'),\n" +
                    "       ('Novikov');\n" +
                    "INSERT INTO film (name, description, releaseDate, duration, rate, ratingId)\n" +
                    "VALUES ('film1', 'description1', '1910-01-01', 111, 12, 1),\n" +
                    "       ('film2', 'description2', '1920-02-02', 122, 14, 2),\n" +
                    "       ('film3', 'description3', '1930-03-03', 133, 8, 3),\n" +
                    "       ('film4', 'description4', '1940-04-04', 144, 1, 4),\n" +
                    "       ('film5', 'description5', '1950-01-05', 155, 0, 5),\n" +
                    "       ('film6', 'description6', '1960-01-05', 166, 0, 1),\n" +
                    "       ('film7', 'description7', '1970-01-05', 177, 8, 2),\n" +
                    "       ('film8', 'description8', '1980-01-05', 188, 0, 3),\n" +
                    "       ('film9', 'description9', '1980-01-05', 188, 0, 4),\n" +
                    "       ('film10', 'description10', '1980-01-05', 188, 12, 5);\n" +
                    "INSERT INTO likes (filmId, userId, mark)\n" +
                    "VALUES (1, 1, 1),\n" +
                    "       (1, 2, 9),\n" +
                    "       (1, 4, 2),\n" +
                    "       (1, 5, 6),\n" +
                    "       (1, 7, 7),\n" +
                    "       (1, 8, 2),\n" +
                    "       (2, 1, 9),\n" +
                    "       (2, 2, 2),\n" +
                    "       (2, 5, 5),\n" +
                    "       (2, 7, 6),\n" +
                    "       (2, 10, 1),\n" +
                    "       (3, 1, 3),\n" +
                    "       (3, 2, 9),\n" +
                    "       (3, 3, 10),\n" +
                    "       (3, 5, 7),\n" +
                    "       (3, 7, 10),\n" +
                    "       (3, 10, 7),\n" +
                    "       (4, 1, 7),\n" +
                    "       (4, 2, 3),\n" +
                    "       (4, 4, 9),\n" +
                    "       (4, 5, 8),\n" +
                    "       (4, 7, 7),\n" +
                    "       (4, 9, 2),\n" +
                    "       (5, 1, 1),\n" +
                    "       (5, 2, 10),\n" +
                    "       (5, 5, 7),\n" +
                    "       (5, 6, 3),\n" +
                    "       (5, 9, 9),\n" +
                    "       (5, 10, 9),\n" +
                    "       (6, 1, 10),\n" +
                    "       (6, 2, 3),\n" +
                    "       (6, 4, 6),\n" +
                    "       (6, 9, 1),\n" +
                    "       (6, 10, 5),\n" +
                    "       (7, 1, 3),\n" +
                    "       (7, 2, 8),\n" +
                    "       (7, 6, 2),\n" +
                    "       (7, 7, 10),\n" +
                    "       (7, 9, 8),\n" +
                    "       (7, 10, 6),\n" +
                    "       (8, 1, 8),\n" +
                    "       (8, 2, 2),\n" +
                    "       (8, 7, 6),\n" +
                    "       (8, 8, 8),\n" +
                    "       (9, 1, 3),\n" +
                    "       (9, 2, 7),\n" +
                    "       (9, 3, 9),\n" +
                    "       (9, 6, 1),\n" +
                    "       (9, 8, 5),\n" +
                    "       (9, 10, 4),\n" +
                    "       (10, 1, 9),\n" +
                    "       (10, 2, 1),\n" +
                    "       (10, 8, 10);\n" +

                    "INSERT INTO directorline (filmId, directorId)\n" +
                    "VALUES (1, 1),\n" +
                    "       (1, 2),\n" +
                    "       (2, 3),\n" +
                    "       (3, 4),\n" +
                    "       (4, 2),\n" +
                    "       (4, 3),\n" +
                    "       (5, 2);" +
                    "update FILM set RATE = (select AVG(MARK) from LIKES where LIKES.FILMID = FILM.FILMID );";
}
