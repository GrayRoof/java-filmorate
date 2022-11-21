package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
public class DirectorController {
private final DirectorService directorService;

@Autowired
    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public Collection<Director> getAllDirector(){
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirector(@PathVariable Integer id){
        return directorService.getDirector(id);
    }

    @PostMapping
    public Director addDirector(@RequestBody Director director){
        return directorService.addDirector(director);
    }

    @PutMapping
    public Director updateDirector(@RequestBody Director director){
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public boolean deleteDirector(@PathVariable Integer id){
        return directorService.deleteDirector(id);
    }

}
