package com.bernardoms.user.controller;

import com.bernardoms.user.dto.UserDTO;
import com.bernardoms.user.exception.NicknameAlreadyExistException;
import com.bernardoms.user.exception.UserNotFoundException;
import com.bernardoms.user.model.UserFilter;
import com.bernardoms.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> saveUser(@RequestBody @Validated UserDTO userDTO, UriComponentsBuilder uriComponentsBuilder) throws NicknameAlreadyExistException {
        var savedNickname = userService.saveUser(userDTO);

        var uriComponent = uriComponentsBuilder.path("/v1/users/{nickname}").buildAndExpand(savedNickname);

        return ResponseEntity.created(uriComponent.toUri()).build();
    }

    @GetMapping("/{nickname}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO getUserByNickName(@PathVariable String nickname) throws UserNotFoundException {
        return userService.getByNickName(nickname);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<UserDTO> getUsers(UserFilter userFilter) {
        return userService.getUsers(userFilter);
    }

    @PutMapping("/{nickname}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUser(@RequestBody UserDTO userDTO, @PathVariable String nickname) throws JsonProcessingException {
        userService.updateUser(nickname, userDTO);
    }

    @DeleteMapping("/{nickname}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String nickname) {
        userService.deleteUser(nickname);
    }
}
