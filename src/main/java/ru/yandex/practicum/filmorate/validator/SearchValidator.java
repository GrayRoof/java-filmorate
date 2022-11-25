package ru.yandex.practicum.filmorate.validator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.WrongSearchException;
import ru.yandex.practicum.filmorate.service.FilmSearchOptions;

import java.util.Set;

@Component
public class SearchValidator {
    public void validateQuery(String query){
        if (query.equals("")) {
            throw new WrongSearchException("Должна быть указана строка запроса query");
        }
    }

    public void validateBy(Set<String> by){
        if (by.isEmpty()) {
            throw new WrongSearchException("Должен быть указан параметр запроса by");
        }
        for (String b: by) {
            if (!FilmSearchOptions.has(b)) {
                throw new WrongSearchException("Параметр запроса by должен соответствовать возможным значениям");
            }
        }
    }

}
