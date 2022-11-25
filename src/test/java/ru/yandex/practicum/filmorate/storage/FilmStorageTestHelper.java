package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class FilmStorageTestHelper {

    private final FilmStorage storage;
    private int nextIdx = 0;

    public FilmStorageTestHelper(FilmStorage storage) {
        this.storage = storage;
    }

    public int getNewFilmId() {
        return addFilm(1, List.of(), List.of()).getId();
    }

    public Film addFilm(int mpaId, Collection<Integer> genreIds, Collection<Integer> directorsIds) {
        int idx = nextIdx++;

        return storage.addFilm(
                new Film(-1,
                        "name-" + idx,
                        "description-" + idx,
                        LocalDate.now().minusYears(10 + idx),
                        100L + idx,
                        0,
                        createMpaLight(mpaId),
                        createGenresLight(genreIds),
                        createDirectorsLight(directorsIds),
                        List.of()
                )
        );
    }

    private Mpa createMpaLight(int id) {
        return new Mpa(id, null, null);
    }

    private Genre createGenreLight(int id) {
        return new Genre(id, null);
    }

    private LinkedHashSet<Genre> createGenresLight(Collection<Integer> ids) {
        return new LinkedHashSet<>(ids.stream().map(this::createGenreLight).collect(Collectors.toList()));
    }

    private Director createDirectorLight(int id) {
        return new Director(id, null);
    }

    private LinkedHashSet<Director> createDirectorsLight(Collection<Integer> ids) {
        return new LinkedHashSet<>(ids.stream().map(this::createDirectorLight).collect(Collectors.toList()));
    }
}
