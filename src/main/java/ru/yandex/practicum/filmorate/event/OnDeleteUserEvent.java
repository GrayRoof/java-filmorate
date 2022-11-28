package ru.yandex.practicum.filmorate.event;

public class OnDeleteUserEvent {
    private final int userId;

    public OnDeleteUserEvent(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }
}
