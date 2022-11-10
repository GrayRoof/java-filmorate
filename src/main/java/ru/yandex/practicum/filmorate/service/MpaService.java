package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dao.DBMpaStorage;

import java.util.Collection;

@Service
public class MpaService {
    private final DBMpaStorage dbMpaStorage;

    public MpaService(DBMpaStorage dbMpaStorage) {
        this.dbMpaStorage = dbMpaStorage;
    }

    public Collection<Mpa> getAllMpa() {
       return dbMpaStorage.getAllMpa();
    }

    public Mpa getMpa(String supposedId) {
        int id = intFromString(supposedId);
        return dbMpaStorage.getMpaById(id);
    }

    private Integer intFromString(final String supposedInt) {
        try {
            return Integer.valueOf(supposedInt);
        } catch (NumberFormatException exception) {
            return Integer.MIN_VALUE;
        }
    }
}
