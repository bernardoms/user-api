package com.bernardoms.user.service;

import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.model.PublishRequest;
import com.bernardoms.user.config.UserQueueConfig;
import com.bernardoms.user.dto.UserDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserNotifyServiceImpl implements UserNotifyService {
    private final UserQueueConfig userQueueConfig;
    private final AmazonSNSAsync amazonSNSAsync;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(UserDTO userDTO) throws JsonProcessingException {
        log.info("user was updated, notifying to sns " + userDTO);
        PublishRequest publishRequest = new PublishRequest(userQueueConfig.getTopic(), objectMapper.writeValueAsString(userDTO));
        amazonSNSAsync.publish(publishRequest);
    }
}
