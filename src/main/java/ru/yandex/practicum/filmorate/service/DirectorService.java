package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import javax.validation.ValidationException;
import java.util.Collection;

@Service
public class DirectorService {
    private final DirectorStorage directorStorage;

    @Autowired
    public DirectorService(
            @Qualifier(UsedStorageConsts.QUALIFIER) DirectorStorage directorStorage
    ) {
        this.directorStorage = directorStorage;
    }

    public Director getDirector(Integer id) {
        return directorStorage.getDirector(id);
    }

    public Collection<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public Director addDirector(Director director) {
        if (getAllDirectors().contains(director)) {
            throw new ConflictException("Режиссер с именем " + director.getName() + " уже существует");
        }
        validateName(director);
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        validateName(director);
        return directorStorage.updateDirector(director);
    }

    public boolean deleteDirector(Integer id) {
        return directorStorage.deleteDirector(id);
    }

    private boolean validateName(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            throw new ValidationException("Имя режиссера не может быть пустым");
        }
        return true;
    }
}
