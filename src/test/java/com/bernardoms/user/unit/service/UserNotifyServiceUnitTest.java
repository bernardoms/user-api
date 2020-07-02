package com.bernardoms.user.unit.service;

import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.model.PublishRequest;
import com.bernardoms.user.config.UserQueueConfig;
import com.bernardoms.user.dto.UserDTO;
import com.bernardoms.user.service.UserNotifyServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserNotifyServiceUnitTest {
    @Mock
    private UserQueueConfig userQueueConfig;
    @Mock
    private AmazonSNSAsync amazonSNSAsync;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private UserNotifyServiceImpl userNotifyServiceImpl;

    @Test
    public void should_send_message_with_user_to_sns() throws JsonProcessingException {
        when(userQueueConfig.getTopic()).thenReturn("test");
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        userNotifyServiceImpl.publish(UserDTO.builder().build());
        verify(amazonSNSAsync, times(1)).publish(new PublishRequest("test", ""));
    }
}
