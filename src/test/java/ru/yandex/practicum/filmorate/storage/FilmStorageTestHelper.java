package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dao.DBFilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class FilmStorageTestHelper {

    private final FilmStorage storage;
    private int nextIdx = 0;

    public FilmStorageTestHelper(DBFilmStorage storage) {
        this.storage = storage;
    }

    public int getNewFilmId() {
        return addFilm(1,List.of()).getId();
    }



    public Film addFilm(int mpaId,Collection<Integer> genreIds) {
        int idx = nextIdx++;

        return storage.addFilm(
                new Film(-1,
                        "name-" + idx,
                        "description-" + idx,
                        LocalDate.now().minusYears(10 + idx),
                        100L + idx,
                        0,
                        createMpaLight(mpaId),
                        createFilmGenresLight(genreIds),
                        new LinkedHashSet<>(),
                        List.of()
                )
        );
    }

    private Mpa createMpaLight(int mpaId) {
        return new Mpa(mpaId,null, null);
    }

    private Genre createFilmGenreLight(int genreId) {
        return new Genre(genreId, null);
    }

    private LinkedHashSet<Genre> createFilmGenresLight(Collection<Integer> genreIds) {

        return new LinkedHashSet<>(genreIds.stream().map(this::createFilmGenreLight).collect(Collectors.toList()));
    }

}
