package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final UserService userService;
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    public RecommendationService(
            UserService userService,
            @Qualifier(UsedStorageConsts.QUALIFIER) FilmStorage filmStorage,
            FilmService filmService) {
        this.userService = userService;
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    /**
     * Возвращает коллекцию фильмов, рекомендованных заданному пользователю другими
     * пользователем(пользователями) с наибольшим пересечением с ним по лайкам к фильмам
     * @param supposedUserId - идентификатор пользователя
     * */
    public Collection<Film> getRecommendations(final String supposedUserId) {
        int userId = userService.getStoredUserId(supposedUserId);

        Map<Integer, BitSet> likes = filmStorage.getRelatedLikesByUserId(userId);
        if (likes.isEmpty() || likes.get(userId).isEmpty()) return List.of();

        BitSet requestUserLikes = likes.remove(userId);
        List<Integer> similarUsers = getSimilarUsers(requestUserLikes, likes);
        if (similarUsers.isEmpty()) { return List.of(); }

        BitSet likesOfUserList = filmStorage.getLikesOfUserList(similarUsers);
        likesOfUserList.andNot(requestUserLikes);
        if (likesOfUserList.cardinality() == 0) { return List.of(); }

        Collection<Film> films = filmStorage.getByIds(
                likesOfUserList.stream().boxed().collect(Collectors.toList()));
        if(!films.isEmpty()) filmService.addExtraFilmData(films);
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
