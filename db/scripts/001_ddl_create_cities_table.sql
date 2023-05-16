CREATE TABLE cities
(
    id      serial PRIMARY KEY,
    name    varchar NOT NULL UNIQUE
);