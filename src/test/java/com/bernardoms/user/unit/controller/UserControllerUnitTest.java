package com.bernardoms.user.unit.controller;

import com.bernardoms.user.controller.ExceptionController;
import com.bernardoms.user.controller.UserController;
import com.bernardoms.user.dto.UserDTO;
import com.bernardoms.user.exception.NicknameAlreadyExistException;
import com.bernardoms.user.exception.UserNotFoundException;
import com.bernardoms.user.model.UserFilter;
import com.bernardoms.user.service.UserQueryService;
import com.bernardoms.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
public class UserControllerUnitTest {
    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    private static final String URL_PATH = "/v1/users";

    @BeforeEach
    public void setUp() {
        mockMvc = standaloneSetup(userController).setControllerAdvice(ExceptionController.class).setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    public void should_return_created_with_location_when_creating_new_user() throws Exception {

        var mapper = new ObjectMapper();

        var newUser = UserDTO.builder().country("UK")
                .email("newusertest@test.com")
                .firstName("new")
                .lastName("user")
                .nickname("newuser")
                .password("newpassword")
                .build();

        when(userService.saveUser(any(UserDTO.class))).thenReturn("newuser");

        mockMvc.perform(
                post(URL_PATH).content(mapper.writeValueAsString(newUser)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andExpect(header().string("Location","http://localhost/v1/users/newuser"));
    }

    @Test
    public void should_return_ok_when_find_existing_user_by_nick_name() throws Exception {

        var user = UserDTO.builder().country("UK")
                .email("test@test.com")
                .firstName("test")
                .lastName("testing")
                .nickname("test_nick").build();

        when(userService.getByNickName("test_nick")).thenReturn(user);

        mockMvc.perform(get(URL_PATH + "/test_nick"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("firstName", is("test")))
                .andExpect(jsonPath("lastName", is("testing")))
                .andExpect(jsonPath("nickname", is("test_nick")))
                .andExpect(jsonPath("email", is("test@test.com")))
                .andExpect(jsonPath("country", is("UK")));
    }

    @Test
    public void should_return_not_found_when_nick_name_dont_exist() throws Exception {
        when(userService.getByNickName("test_nick4")).thenThrow(new UserNotFoundException("user not found"));
        mockMvc.perform(get(URL_PATH + "/test_nick4")).andExpect(status().isNotFound());
    }

    @Test
    public void should_return_bad_request_when_creating_new_user_invalid_body() throws Exception {
        var mapper = new ObjectMapper();

        var newUser = UserDTO.builder().country("UK")
                .email("newusertest").build();

        mockMvc.perform(post(URL_PATH).content(mapper.writeValueAsString(newUser)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description.lastName", is("must not be blank")))
                .andExpect(jsonPath("$.description.firstName", is("must not be blank")))
                .andExpect(jsonPath("$.description.password", is("must not be blank")))
                .andExpect(jsonPath("$.description.nickname", is("must not be blank")))
                .andExpect(jsonPath("$.description.email", is("must be a well-formed email address")));
    }

    @Test
    public void should_return_unprocesable_entity_when_creating_new_user_with_an_already_existing_nick_name() throws Exception {
        var mapper = new ObjectMapper();

        when(userService.saveUser(any(UserDTO.class))).thenThrow(new NicknameAlreadyExistException("user with nick name test_nick already exist!"));

        var newUser = UserDTO.builder().country("UK")
                .email("newusertest@test.com")
                .firstName("new")
                .lastName("user")
                .nickname("test_nick")
                .password("newpassword")
                .build();

        mockMvc.perform(post(URL_PATH).content(mapper.writeValueAsString(newUser)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.description", is("user with nick name test_nick already exist!")));
    }

    @Test
    public void should_return_no_content_when_deleting_an_user() throws Exception {
        mockMvc.perform(delete(URL_PATH + "/test_nick2")).andExpect(status().isNoContent());
    }

    @Test
    public void should_return_no_content_when_updating_user() throws Exception {
        var mapper = new ObjectMapper();

        var updateUser = UserDTO.builder().country("UK")
                .email("updateUsertest@test.com").build();

        mockMvc.perform(put(URL_PATH + "/test_nick")
                .content(mapper.writeValueAsString(updateUser))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void should_return_ok_when_get_users_with_filter() throws Exception {


        var user = UserDTO.builder().country("BR")
                .email("test3@test.com")
                .firstName("test2")
                .lastName("testing2")
                .nickname("test_nick3").build();

        var pageable = PageRequest.of(0,100, Sort.unsorted());

        var pageableUser = new PageImpl<>(Collections.singletonList(user), pageable, 0);

        when(userService.getUsers(any(UserFilter.class))).thenReturn(pageableUser);

        mockMvc.perform(get(URL_PATH)
                .param("firstName", "test2")
                .param("lastName", "testing2")
                .param("nickname", "test_nick3")
                .param("country", "BR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName", is("test2")))
                .andExpect(jsonPath("$.content[0].lastName", is("testing2")))
                .andExpect(jsonPath("$.content[0].nickname", is("test_nick3")))
                .andExpect(jsonPath("$.content[0].email", is("test3@test.com")))
                .andExpect(jsonPath("$.content[0].country", is("BR")));
    }
}
