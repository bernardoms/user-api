package com.bernardoms.user.service;


import com.bernardoms.user.dto.UserDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface UserNotifyService {
    void publish(UserDTO userDTO) throws JsonProcessingException;
}
