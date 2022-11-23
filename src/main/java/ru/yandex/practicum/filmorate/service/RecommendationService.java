package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Service
public class RecommendationService {

    private final UserService userService;
    private final FilmStorage filmStorage;

    public RecommendationService(
            UserService userService,
            @Qualifier(UsedStorageConsts.QUALIFIER) FilmStorage filmStorage
    ) {
        this.userService = userService;
        this.filmStorage = filmStorage;
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

        return filmStorage.getFilmsOfIdArray(likesOfUserList.toString().replace("{", "").replace("}", ""));
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
