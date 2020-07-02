package com.bernardoms.user.service;

import com.bernardoms.user.dto.UserDTO;
import com.bernardoms.user.exception.NicknameAlreadyExistException;
import com.bernardoms.user.exception.UserNotFoundException;
import com.bernardoms.user.model.User;
import com.bernardoms.user.model.UserFilter;
import com.bernardoms.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserQueryService userQueryService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserNotifyService userNotifyService;

    @Cacheable(cacheNames = "user")
    public UserDTO getByNickName(String nickName) throws UserNotFoundException {
        User user = userRepository.findByNickname(nickName).orElseThrow(() -> new UserNotFoundException("user with nick name " + nickName + " not found!"));
        return modelMapper.map(user, UserDTO.class);
    }

    public Page<UserDTO> getUsers(UserFilter userFilter) {
        var users = userQueryService.returnPageableFilteredUser(userFilter);
        return users.map(u -> new UserDTO(u.getNickname(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getCountry()));
    }

    public String saveUser(UserDTO userDTO) throws NicknameAlreadyExistException {
        var optionalUser = userRepository.findByNickname(userDTO.getNickname());
        if (optionalUser.isPresent()) {
            throw new NicknameAlreadyExistException("user with nick name " + userDTO.getNickname() + " already exist!");
        }
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        return userRepository.save(modelMapper.map(userDTO, User.class)).getNickname();
    }

    @CacheEvict(cacheNames = "user")
    public void deleteUser(String nickname) {
        Optional<User> optionalUser = userRepository.findByNickname(nickname);
        optionalUser.ifPresent(user -> userRepository.deleteById(user.getId()));
        log.info("user " + nickname + " deleted!");
    }

    @CacheEvict(cacheNames = "user", key = "#nickname")
    public void updateUser(String nickname, UserDTO userDTO) throws JsonProcessingException {
        var userOptional = userRepository.findByNickname(nickname);
        if (userOptional.isPresent()) {
            userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            User updatedUser = modelMapper.map(userDTO, User.class);
            updatedUser.setId(userOptional.get().getId());
            userRepository.save(updatedUser);
            userNotifyService.publish(userDTO);
        }
    }
}
