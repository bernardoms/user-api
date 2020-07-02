package com.bernardoms.user.unit.service;

import com.bernardoms.user.dto.UserDTO;
import com.bernardoms.user.exception.NicknameAlreadyExistException;
import com.bernardoms.user.exception.UserNotFoundException;
import com.bernardoms.user.model.User;
import com.bernardoms.user.model.UserFilter;
import com.bernardoms.user.repository.UserRepository;
import com.bernardoms.user.service.UserNotifyService;
import com.bernardoms.user.service.UserNotifyServiceImpl;
import com.bernardoms.user.service.UserQueryService;
import com.bernardoms.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserQueryService userQueryService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserNotifyService userNotifyService;
    @InjectMocks
    private UserService userService;

    @Test
    public void should_return_user_when_find_by_existing_nickname() throws UserNotFoundException {
        var user = User.builder().country("UK")
                .email("test@test.com")
                .firstName("test")
                .lastName("unit")
                .nickname("test")
                .build();

        when(modelMapper.map(user, UserDTO.class)).thenReturn(UserDTO.builder().country("UK")
                .email("test@test.com")
                .firstName("test")
                .lastName("unit")
                .nickname("test")
                .build());

        when(userRepository.findByNickname("test_nickname")).thenReturn(Optional.of(user));
        var userDTO = userService.getByNickName("test_nickname");
        assertEquals(user.getNickname(), userDTO.getNickname());
        assertEquals(user.getCountry(), userDTO.getCountry());
        assertEquals(user.getEmail(), userDTO.getEmail());
        assertEquals(user.getFirstName(), userDTO.getFirstName());
        assertEquals(user.getLastName(), userDTO.getLastName());
    }

    @Test
    public void should_throw_user_not_found_exception_when_user_not_found() {
        var exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getByNickName("test_nickname");
        });
        assertEquals("user with nick name test_nickname not found!", exception.getMessage());
    }

    @Test
    public void should_return_list_of_users_filtered_by_parameter() {
        var userFilter = UserFilter.builder().country("UK")
                .email("test@test.com")
                .firstName("first name")
                .lastName("last name")
                .nickname("nick name")
                .build();

        var user = User.builder().country("UK")
                .email("test@test.com")
                .firstName("first name")
                .lastName("last name")
                .nickname("nick name")
                .build();

        var pageable = PageRequest.of(0, 100, Sort.unsorted());

        var pageableUser = new PageImpl<>(Collections.singletonList(user), pageable, 0);

        when(userQueryService.returnPageableFilteredUser(userFilter)).thenReturn(pageableUser);

        var users = userService.getUsers(userFilter);

        assertEquals(1, users.get().count());
        assertEquals(1, users.getTotalPages());
    }

    @Test
    public void should_save_user_and_return_nickname_of_saved_user() throws NicknameAlreadyExistException {
        var userDTO = UserDTO.builder().country("UK")
                .email("test@test.com")
                .firstName("test")
                .lastName("unit")
                .nickname("test")
                .password("newpassword")
                .build();

        var user = User.builder().country("UK")
                .email("test@test.com")
                .firstName("test")
                .lastName("unit")
                .nickname("test")
                .build();

        when(userRepository.save(user)).thenReturn(user);

        when(modelMapper.map(userDTO, User.class)).thenReturn(user);

        String nickname = userService.saveUser(userDTO);

        verify(passwordEncoder, times(1)).encode("newpassword");

        assertEquals("test", nickname);
    }

    @Test
    public void should_throw_nick_name_already_exist_when_saving_a_user_with_existing_nick() {
        var userDTO = UserDTO.builder().country("UK")
                .email("test@test.com")
                .firstName("test")
                .lastName("unit")
                .nickname("test")
                .password("newpassword")
                .build();

        var user = User.builder().country("UK")
                .email("test@test.com")
                .firstName("test")
                .lastName("unit")
                .nickname("test")
                .build();

        when(userRepository.findByNickname("test")).thenReturn(Optional.of(user));

        Exception exception = assertThrows(NicknameAlreadyExistException.class, () -> {
            userService.saveUser(userDTO);
            verify(passwordEncoder, never()).encode("newpassword");
            verify(modelMapper, never()).map(userDTO, User.class);
        });

        assertEquals("user with nick name test already exist!", exception.getMessage());
    }

    @Test
    public void should_delete_an_user_if_user_exists() {
        var user = User.builder().country("UK")
                .id(new ObjectId("507f191e810c19729de860ea"))
                .email("test@test.com")
                .firstName("test")
                .lastName("unit")
                .nickname("test")
                .build();

        when(userRepository.findByNickname("test")).thenReturn(Optional.of(user));

        userService.deleteUser("test");

        verify(userRepository, times(1)).deleteById(new ObjectId("507f191e810c19729de860ea"));
    }

    @Test
    public void should_not_call_repository_to_delete_when_user_not_exist() {
        userService.deleteUser("test");

        verify(userRepository, never()).deleteById(any(ObjectId.class));
    }

    @Test
    public void should_update_user_and_notify_if_user_already_exist() throws JsonProcessingException {
        var userDTO = UserDTO.builder().country("UK")
                .email("test@test.com")
                .firstName("test")
                .lastName("unit")
                .nickname("testnewnick")
                .password("newpassword")
                .build();

        var user = User.builder().country("UK")
                .id(new ObjectId("507f191e810c19729de860ea"))
                .email("test@test.com")
                .firstName("test")
                .lastName("unit")
                .nickname("test")
                .build();

        when(userRepository.findByNickname("test")).thenReturn(Optional.of(user));

        when(modelMapper.map(userDTO, User.class)).thenReturn(user);

        userService.updateUser("test", userDTO);

        verify(passwordEncoder, times(1)).encode("newpassword");
        verify(userRepository, times(1)).save(user);
        verify(userNotifyService, times(1)).publish(userDTO);
    }

    @Test
    public void should_not_notify_nor_update_if_user_not_exist() throws JsonProcessingException {
        var userDTO = UserDTO.builder().country("UK")
                .email("test@test.com")
                .firstName("test")
                .lastName("unit")
                .nickname("testnewnick")
                .password("newpassword")
                .build();

        userService.updateUser("test", userDTO);

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(userNotifyService, never()).publish(any(UserDTO.class));
    }
}
