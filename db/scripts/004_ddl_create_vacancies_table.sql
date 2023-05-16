CREATE TABLE vacancies
(
    id              serial PRIMARY KEY,
    title           varchar NOT NULL,
    description     varchar NOT NULL,
    creation_date   timestamp,
    visible         boolean NOT NULL,
    city_id         int REFERENCES cities(id),
    file_id         int REFERENCES files(id)
);