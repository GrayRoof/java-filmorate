TRUNCATE TABLE LIKES;
TRUNCATE TABLE GENRELINE;
TRUNCATE TABLE FRIENDSHIP;
TRUNCATE TABLE DIRECTORLINE;
TRUNCATE TABLE USEFUL;
TRUNCATE TABLE DIRECTORS;
TRUNCATE TABLE EVENTS;
TRUNCATE TABLE REVIEWS;
TRUNCATE TABLE USERS;
TRUNCATE TABLE FILM;

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