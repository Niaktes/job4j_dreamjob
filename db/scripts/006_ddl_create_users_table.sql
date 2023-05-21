CREATE TABLE users
(
    id          serial PRIMARY KEY,
    email       varchar UNIQUE NOT NULL,
    name        varchar NOT NULL,
    password    varchar NOT NULL
);