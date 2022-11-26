package ru.yandex.practicum.filmorate.service;

public enum FilmSearchOptions {
    DIRECTOR,
    TITLE;

    static public boolean has(String value) {
        if (value.equals(null)) { return false; }
        try {
            valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException x) {
            return false;
        }
    }
}
