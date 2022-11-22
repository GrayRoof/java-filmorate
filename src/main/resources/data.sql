DELETE FROM LIKES;
DELETE FROM GENRELINE;
DELETE FROM FRIENDSHIP;
DELETE FROM DIRECTORLINE;
DELETE FROM USEFUL;
DELETE FROM DIRECTORS;
DELETE FROM EVENTS;
DELETE FROM REVIEWS;
DELETE FROM USERS;
DELETE FROM FILM;

ALTER TABLE USERS ALTER COLUMN USERID RESTART WITH 1;
ALTER TABLE FILM ALTER COLUMN FILMID RESTART WITH 1;
ALTER TABLE EVENTS ALTER COLUMN EVENTID RESTART WITH 1;
ALTER TABLE REVIEWS ALTER COLUMN REVIEWID RESTART WITH 1;


MERGE INTO MPA KEY(RATINGID)
    VALUES (1, 'G', 'Нет возрастных ограничений'),
           (2, 'PG', 'Рекомендуется присутствие родителей'),
           (3, 'PG-13', 'Детям до 13 лет просмотр не желателен'),
           (4, 'R', 'Лицам до 17 лет обязательно присутствие взрослого'),
           (5, 'NC-17', 'Лицам до 18 лет просмотр запрещен');

MERGE INTO GENRE KEY(GENREID)
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

--Для отработки Рекомендаций
/*INSERT INTO users (email, login, name, birthday)
    VALUES ('1@1.com', 'user1', 'name1', '2001-01-01'),
           ('2@2.com', 'user2', 'name2', '2002-02-02'),
           ('3@3.com', 'user3', 'name3', '2003-03-03'),
           ('4@4.com', 'user4', 'name4', '2004-04-04'),
           ('5@5.com', 'user5', 'name5', '2001-01-05');

INSERT INTO film (name, description, releaseDate, duration, ratingId)
    VALUES ('film1', 'description1', '1910-01-01', 111, 1),
           ('film2', 'description2', '1920-02-02', 122, 2),
           ('film3', 'description3', '1930-03-03', 133, 3),
           ('film4', 'description4', '1940-04-04', 144, 4),
           ('film5', 'description5', '1950-01-05', 155, 5);

INSERT INTO likes (filmId, userId)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (2, 1),
       (2, 2),
       (2, 3),
       (2, 4),
       (3, 3),
       (3, 5),
       (4, 1),
       (5, 2),
       (5, 4),
       (5, 5);*/

