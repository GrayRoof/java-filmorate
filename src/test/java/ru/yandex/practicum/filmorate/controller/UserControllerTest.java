package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserController userController;

    @Test
    void shouldReturn200whenGetUsers() throws Exception {
        User user = new User();
        user.setLogin("correctlogin");
        user.setName("Correct Name");
        user.setEmail("correct.email@mail.ru");
        user.setBirthday(LocalDate.of(2002, 1, 1));
        Mockito.when(userController.findAll()).thenReturn(Collections.singletonList(user));
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Collections.singletonList(user))));
    }

    @Test
    void shouldReturn200whenPostCorrectUserData() throws Exception {
        User user = new User();
        user.setLogin("correctlogin");
        user.setName("Correct Name");
        user.setEmail("correct.email@mail.ru");
        user.setBirthday(LocalDate.of(2002, 1, 1));
        Mockito.when(userController.create(Mockito.any())).thenReturn(user);
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(user)));
    }

    @Test
    void shouldReturn400whenPostFailedUserLogin() throws Exception {
        User user = new User();
        user.setLogin("incorrect login");
        user.setName("Correct Name");
        user.setEmail("correct.email@mail.ru");
        user.setBirthday(LocalDate.of(2002, 1, 1));
        Mockito.when(userController.create(Mockito.any())).thenReturn(user);
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldReturn400whenPostFailedUserEmail() throws Exception {
        User user = new User();
        user.setLogin("correctlogin");
        user.setName("Correct Name");
        user.setEmail("incorrect.email@");
        user.setBirthday(LocalDate.of(2002, 1, 1));
        Mockito.when(userController.create(Mockito.any())).thenReturn(user);
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldReturn400whenPostFailedUserBirthDate() throws Exception {
        User user = new User();
        user.setLogin("correctlogin");
        user.setName("Correct Name");
        user.setEmail("correct.email@mail.ru");
        user.setBirthday(LocalDate.now().plusDays(1));
        Mockito.when(userController.create(Mockito.any())).thenReturn(user);
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}