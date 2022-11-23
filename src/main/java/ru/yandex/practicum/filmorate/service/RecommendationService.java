package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.*;

@Service
public class RecommendationService {

    private final UserService userService;
    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;

    public RecommendationService(
            UserService userService,
            @Qualifier(UsedStorageConsts.QUALIFIER) FilmStorage filmStorage,
            @Qualifier(UsedStorageConsts.QUALIFIER) GenreStorage genreStorage,
            @Qualifier(UsedStorageConsts.QUALIFIER) DirectorStorage directorStorage
    ) {
        this.userService = userService;
        this.filmStorage = filmStorage;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
    }

    /**
     * Возвращает коллекцию фильмов, рекомендованных заданному пользователю другими
     * пользователем(пользователями) с наибольшим пересечением с ним по лайкам к фильмам
     * @param supposedUserId - идентификатор пользователя
     * */
    public Collection<Film> getRecommendations(final String supposedUserId) {
        User user = userService.getStoredUser(supposedUserId);
        int userId = user.getId();

        Map<Integer, BitSet> likes = filmStorage.getRelatedLikesByUserId(userId);
        if (likes.isEmpty() || likes.get(userId).isEmpty()) return List.of();

        BitSet requestUserLikes = likes.remove(userId);
        List<Integer> similarUsers = getSimilarUsers(requestUserLikes, likes);
        if (similarUsers.isEmpty()) { return List.of(); }

        BitSet likesOfUserList = filmStorage.getLikesOfUserList(similarUsers);
        likesOfUserList.andNot(requestUserLikes);
        if (likesOfUserList.cardinality() == 0) { return List.of(); }

        Collection<Film> films = filmStorage.getFilmsOfIdArray(
                likesOfUserList
                        .toString()
                        .replace("{", "")
                        .replace("}", "")
        );
        directorStorage.load(films);
        genreStorage.load(films);
        return films;
    }

    private List<Integer> getSimilarUsers(BitSet requestUserLikes, Map<Integer, BitSet> likes) {
        List<Integer> similarUsers = new ArrayList<>();
        int maximumMatches = 0;
        for (Integer key: likes.keySet()) {
            likes.get(key).and(requestUserLikes);
            int numberOfMatches = likes.get(key).cardinality();
            if (numberOfMatches >= maximumMatches) {
                if (numberOfMatches > maximumMatches) { similarUsers.clear(); }
                similarUsers.add(key);
                maximumMatches = numberOfMatches;
            }
        }
        return similarUsers;
    }

}
