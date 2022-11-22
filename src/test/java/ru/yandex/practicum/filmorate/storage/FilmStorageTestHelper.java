package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public class FilmStorageTestHelper {

    private final FilmStorage storage;
    private int nextIdx = 0;

    public FilmStorageTestHelper(FilmStorage storage) {
        this.storage = storage;
    }

    public int getNewFilmId() {
        return addFilm(1, List.of(), List.of()).getId();
    }

    public Film addFilm(int mpaId, Collection<Integer> genreIds, Collection<Integer> directorIds) {
        int idx = nextIdx++;

        return storage.addFilm(
                new Film(-1,
                        "name-" + idx,
                        "description-" + idx,
                        LocalDate.now().minusYears(10 + idx),
                        100L + idx,
                        0,
                        createMpaLight(mpaId),
                        new LinkedHashSet<>(),
                        new LinkedHashSet<>(),
                        List.of()
                )
        );
    }

    private Mpa createMpaLight(int mpaId) {
        return new Mpa(mpaId, null, null);
    }


}
