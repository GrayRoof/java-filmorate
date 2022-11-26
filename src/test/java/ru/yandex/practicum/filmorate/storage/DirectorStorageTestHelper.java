package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

public class DirectorStorageTestHelper {
    private final DirectorStorage storage;
    private int nextIdx = 0;

    public DirectorStorageTestHelper(DirectorStorage storage) {
        this.storage = storage;
    }

    public int getNewDirectorId() {
        return addDirector().getId();
    }

    public Director addDirector() {
        int idx = nextIdx++;

        return storage.add(
                new Director(-1,
                        String.format("name-%d", idx)
                )
        );
    }
}