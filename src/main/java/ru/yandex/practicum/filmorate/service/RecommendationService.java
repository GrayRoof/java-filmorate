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
     * пользователем(пользователями) с наибольшим пересечением с ним по оценкам к фильмам
     * @param supposedUserId - идентификатор пользователя
     * */
    public Collection<Film> getRecommendations(final String supposedUserId) {
        int userId = userService.getStoredUserId(supposedUserId);

        Map<Integer, Integer> scores = filmStorage.getScoresOfRelatedLikesByUserId(userId);
        if (scores.isEmpty()) { return List.of(); }

        Map<Integer, List<Integer>> similarUsers = getSimilarUsers(scores);
        if (similarUsers.isEmpty()) { return List.of(); }

        List<Integer> filmIds = new ArrayList<>();
        Iterator<Map.Entry<Integer, List<Integer>>> itr = similarUsers.entrySet().iterator();
        while(itr.hasNext()) {
            Map.Entry<Integer, List<Integer>> entry = itr.next();
            filmIds = filmStorage.getFilmIdsOfUserList(userId, entry.getValue());
            if (!filmIds.isEmpty()) { break; }
        }
        if (filmIds.isEmpty()) { return List.of(); }

        Collection<Film> films = filmStorage.getByIds(filmIds);
        if(!films.isEmpty()) filmService.addExtraFilmData(films);
        return films;
    }

    //Предыдущий код
    /*public Collection<Film> getRecommendations(final String supposedUserId) {
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
    }*/

    private Map<Integer, List<Integer>> getSimilarUsers(Map<Integer, Integer> scores) {
        Map<Integer, List<Integer>> rankedUserLists = new TreeMap<>(Collections.reverseOrder());
        for (Integer key: scores.keySet()) {
            int value = scores.get(key);
            rankedUserLists.putIfAbsent(value, new ArrayList<>());
            rankedUserLists.get(value).add(key);
        }
        return rankedUserLists;
    }

    //код с оценками, но без анализа, имеются ли рекомендованные фильмы
    /*private List<Integer> getSimilarUsers(Map<Integer, Integer> scores) {
        List<Integer> similarUsers = new ArrayList<>();
        int maximumScore = 0;
        for (Integer key: scores.keySet()) {
            int currentScore = scores.get(key);
            if (currentScore >= maximumScore) {
                if (currentScore > maximumScore) { similarUsers.clear(); }
                similarUsers.add(key);
                maximumScore = currentScore;
            }
        }
        return similarUsers;
    }*/

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
