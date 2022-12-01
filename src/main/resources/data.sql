DELETE
FROM LIKES;
DELETE
FROM GENRELINE;
DELETE
FROM FRIENDSHIP;
DELETE
FROM DIRECTORLINE;
DELETE
FROM USEFUL;
DELETE
FROM DIRECTORS;
DELETE
FROM EVENTS;
DELETE
FROM REVIEWS;
DELETE
FROM USERS;
DELETE
FROM FILM;

ALTER TABLE USERS
    ALTER COLUMN USERID RESTART WITH 1;
ALTER TABLE DIRECTORS
    ALTER COLUMN DIRECTORID RESTART WITH 1;
ALTER TABLE FILM
    ALTER COLUMN FILMID RESTART WITH 1;
ALTER TABLE EVENTS
    ALTER COLUMN EVENTID RESTART WITH 1;
ALTER TABLE REVIEWS
    ALTER COLUMN REVIEWID RESTART WITH 1;


MERGE INTO MPA KEY (RATINGID)
    VALUES (1, 'G', 'Нет возрастных ограничений'),
           (2, 'PG', 'Рекомендуется присутствие родителей'),
           (3, 'PG-13', 'Детям до 13 лет просмотр не желателен'),
           (4, 'R', 'Лицам до 17 лет обязательно присутствие взрослого'),
           (5, 'NC-17', 'Лицам до 18 лет просмотр запрещен');

MERGE INTO GENRE KEY (GENREID)
    VALUES (1, 'Комедия'),
           (2, 'Драма'),
           (3, 'Мультфильм'),
           (4, 'Триллер'),
           (5, 'Документальный'),
           (6, 'Боевик');

MERGE INTO EVENTTYPES KEY (TYPEID)
    VALUES (1, 'LIKE'),
           (2, 'REVIEW'),
           (3, 'FRIEND');

MERGE INTO OPERATIONS KEY (OPERATIONID)
    VALUES (1, 'REMOVE'),
           (2, 'ADD'),
           (3, 'UPDATE');

/* INSERT INTO users (email, login, name, birthday)
VALUES ('1@1.com', 'user1', 'name1', '2001-01-01'),
       ('2@2.com', 'user2', 'name2', '2002-02-02'),
       ('3@3.com', 'user3', 'name3', '2003-03-03'),
       ('4@4.com', 'user4', 'name4', '2004-04-04'),
       ('5@5.com', 'user5', 'name5', '2001-01-05'),
       ('0@0.com', 'user0', 'name0', '2000-01-10'),
       ('7@7.com', 'user7', 'name7', '2001-07-17');

INSERT INTO directors (name)
VALUES ('Ivanov'),
       ('Svetlov'),
       ('Svetlanov'),
       ('Novikov');

INSERT INTO film (name, description, releaseDate, duration, rate, ratingId)
VALUES ('film1', 'description1', '1910-01-01', 111, 12, 1),
       ('film2', 'description2', '1920-02-02', 122, 14, 2),
       ('film3', 'description3', '1930-03-03', 133, 8, 3),
       ('film4', 'description4', '1940-04-04', 144, 1, 4),
       ('film5', 'description5', '1950-01-05', 155, 0, 5),
       ('film6', 'description6', '1960-01-05', 166, 0, 1),
       ('film6', 'description6', '1960-01-05', 166, 0, 1),
       ('film7', 'description7', '1970-01-05', 177, 0, 2),
       ('film8', 'description8', '1980-01-05', 188, 0, 3);

INSERT INTO likes (filmId, userId, mark)
VALUES (1, 6, 6),
       (1, 4, 5),
       (1, 5, 8),

       (1, 2, 1),

       (3, 6, 10),
       (3, 1, 10),
       (3, 2, 7),
       (3, 3, 1),
       (3, 4, 1),
       (3, 5, 9),
       (4, 6, 3),
       (4, 4, 9),
       (4, 5, 2),

       (4, 2, 5),

       (5, 6, 8),
       (5, 2, 6),
       (5, 4, 7),
       (5, 5, 7),
       (7, 6, 1),
       (7, 3, 10),
       (7, 4, 5),
       (7, 5, 1);

       (2, 2, 3),
       (6, 3, 8);


INSERT INTO directorline (filmId, directorId)
VALUES (1, 1),
       (1, 2),
       (2, 3),
       (3, 4),
       (4, 2),
       (4, 3),
       (5, 2); */