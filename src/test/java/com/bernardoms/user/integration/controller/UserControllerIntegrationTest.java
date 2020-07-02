package com.bernardoms.user.integration.controller;

import com.amazonaws.services.sns.AmazonSNSAsync;
import com.bernardoms.user.dto.UserDTO;
import com.bernardoms.user.integration.IntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class UserControllerIntegrationTest extends IntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private AmazonSNSAsync amazonSNSAsync;

    private static final String URL_PATH = "/v1/users";

    @Test
    public void should_return_ok_when_find_existing_user_by_nick_name() throws Exception {
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
        mockMvc.perform(get(URL_PATH + "/test_nick4")).andExpect(status().isNotFound());
    }

    @Test
    public void should_return_created_with_location_when_creating_new_user() throws Exception {
        var newUser = UserDTO.builder().country("UK")
                .email("newusertest@test.com")
                .firstName("new")
                .lastName("user")
                .nickname("newuser")
                .password("newpassword").build();

        mockMvc.perform(post(URL_PATH).content(mapper.writeValueAsString(newUser)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andExpect(header().stringValues("location", "http://localhost/v1/users/newuser"));
    }

    @Test
    public void should_return_bad_request_when_creating_new_user_invalid_body() throws Exception {
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
        var newUser = UserDTO.builder().country("UK")
                .email("newusertest@test.com")
                .firstName("new")
                .lastName("user")
                .nickname("test_nick")
                .password("newpassword").build();

        mockMvc.perform(post(URL_PATH).content(mapper.writeValueAsString(newUser)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.description", is("user with nick name test_nick already exist!")));
    }

    @Test
    public void should_return_no_content_when_deleting_an_user() throws Exception {
        mockMvc.perform(delete(URL_PATH + "/test_nick2")).andExpect(status().isNoContent());
    }

    @Test
    public void should_return_no_content_when_updating_user() throws Exception {
        var updateUser = UserDTO.builder().country("UK").password("passowrd")
                .email("updateUsertest@test.com").build();
        mockMvc.perform(put(URL_PATH + "/test_nick")
                .content(mapper.writeValueAsString(updateUser))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void should_return_ok_when_get_users_with_filter() throws Exception {
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

    @Test
    public void should_return_ok_with_all_users_when_get_users_with_no_filters() throws Exception {
        mockMvc.perform(get(URL_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName", is("test")))
                .andExpect(jsonPath("$.content[0].lastName", is("testing")))
                .andExpect(jsonPath("$.content[0].nickname", is("test_nick")))
                .andExpect(jsonPath("$.content[0].email", is("test@test.com")))
                .andExpect(jsonPath("$.content[0].country", is("UK")))
                .andExpect(jsonPath("$.content[1].firstName", is("test2")))
                .andExpect(jsonPath("$.content[1].lastName", is("testing2")))
                .andExpect(jsonPath("$.content[1].nickname", is("test_nick2")))
                .andExpect(jsonPath("$.content[1].email", is("test2@test.com")))
                .andExpect(jsonPath("$.content[1].country", is("BR")))
                .andExpect(jsonPath("$.content[2].firstName", is("test2")))
                .andExpect(jsonPath("$.content[2].lastName", is("testing2")))
                .andExpect(jsonPath("$.content[2].nickname", is("test_nick3")))
                .andExpect(jsonPath("$.content[2].email", is("test3@test.com")))
                .andExpect(jsonPath("$.content[2].country", is("BR")));
    }

    @Test
    public void should_return_ok_with_all_users_when_get_users_with_paging_filter() throws Exception {
        mockMvc.perform(get(URL_PATH).param("offset", "1").param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName", is("test2")))
                .andExpect(jsonPath("$.content[0].lastName", is("testing2")))
                .andExpect(jsonPath("$.content[0].nickname", is("test_nick2")))
                .andExpect(jsonPath("$.content[0].email", is("test2@test.com")))
                .andExpect(jsonPath("$.content[0].email", is("test2@test.com")))
                .andExpect(jsonPath("$.pageable.pageNumber", is(1)))
                .andExpect(jsonPath("$.pageable.offset", is(1)))
                .andExpect(jsonPath("$.last", is(false)));
    }
}
