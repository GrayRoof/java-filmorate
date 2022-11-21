package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

public class UserStorageTestHelper {
    private final UserStorage storage;
    private int nextIdx = 0;

    public UserStorageTestHelper(UserStorage storage) {
        this.storage = storage;
    }

    public User addUser() {
        int idx = nextIdx++;

        return storage.addUser(
                new User(-1,
                        String.format("email-%d", idx),
                        String.format("description-%d", idx),
                        String.format("name-%d", idx),
                        LocalDate.of(1984, 1, 1).plusDays(idx),
                        List.of()
                )
        );
    }
}
