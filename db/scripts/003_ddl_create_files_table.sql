CREATE TABLE files
(
    id      serial PRIMARY KEY,
    name    varchar NOT NULL,
    path    varchar NOT NULL UNIQUE
);