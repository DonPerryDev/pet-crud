CREATE SCHEMA IF NOT EXISTS petapp;

CREATE TABLE petapp.pets (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    species VARCHAR(50) NOT NULL,
    breed VARCHAR(255),
    age INT NOT NULL,
    birthdate DATE,
    weight DECIMAL(10, 2),
    nickname VARCHAR(255),
    owner VARCHAR(255) NOT NULL,
    registration_date DATE NOT NULL,
    photo_url VARCHAR(500)
);
