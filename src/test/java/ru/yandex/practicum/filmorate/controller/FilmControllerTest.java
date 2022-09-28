package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private FilmController filmController;

    @Test
    void shouldReturn200whenGetFilms() throws Exception {
        Film film = Film.builder()
                .name("Correct Name")
                .description("Correct description")
                .releaseDate(LocalDate.of(1995,5,26))
                .duration(100L)
                .build();
        Mockito.when(filmController.findAll()).thenReturn(Collections.singletonList(film));
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Collections.singletonList(film))));
    }

    @Test
    void shouldReturn200whenPostCorrectFilmData() throws Exception {
        Film film = Film.builder()
                .name("Correct Name")
                .description("Correct description")
                .releaseDate(LocalDate.of(1995,5,26))
                .duration(100L)
                .build();
        Mockito.when(filmController.create(Mockito.any())).thenReturn(film);
        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(film)));
    }

    @Test
    void shouldReturn400whenPostFailedFilmNameEmpty() throws Exception {
        Film film = Film.builder()
                .name("")
                .description("Correct description")
                .releaseDate(LocalDate.of(1895,12,28))
                .duration(100L)
                .build();
       // Mockito.when(filmController.create(Mockito.any())).thenReturn(film);
        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldReturn400whenPostFailedFilmNameBlank() throws Exception {
        Film film = Film.builder()
                .name("  ")
                .description("Correct description")
                .releaseDate(LocalDate.of(1895,12,28))
                .duration(100L)
                .build();
        // Mockito.when(filmController.create(Mockito.any())).thenReturn(film);
        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldReturn400whenPostFailedFilmDuration() throws Exception {
        Film film = Film.builder()
                .name("Correct Name")
                .description("Correct description")
                .releaseDate(LocalDate.of(1995,5,26))
                .duration(-100L)
                .build();
        Mockito.when(filmController.create(Mockito.any())).thenReturn(film);
        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldReturn400whenPostFailedFilmReleaseDate() throws Exception {
        Film film = Film.builder()
                .name("Correct Name")
                .description("Correct description")
                .releaseDate(LocalDate.of(1895,12,27))
                .duration(100L)
                .build();
        Mockito.when(filmController.create(Mockito.any())).thenReturn(film);
        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldReturn400whenPostFailedFilmDescription() throws Exception {
        Film film = Film.builder()
                .name("Correct Name")
                .description("Failed description. Failed description. Failed description. Failed description. " +
                        "Failed description. Failed description. Failed description. Failed description. " +
                        "Failed description. Failed description. F")
                .releaseDate(LocalDate.of(1995,5,26))
                .duration(100L)
                .build();
        Mockito.when(filmController.create(Mockito.any())).thenReturn(film);
        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldReturn200whenPostFilmDescription200() throws Exception {
        Film film = Film.builder()
                .name("Correct Name")
                .description("Failed description. Failed description. Failed description. Failed description. " +
                        "Failed description. Failed description. Failed description. Failed description. " +
                        "Failed description. Failed description. ")
                .releaseDate(LocalDate.of(1995,5,26))
                .duration(100L)
                .build();
        Mockito.when(filmController.create(Mockito.any())).thenReturn(film);
        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}