package com.bernardoms.user.unit.service;

import com.bernardoms.user.dto.UserDTO;
import com.bernardoms.user.model.User;
import com.bernardoms.user.model.UserFilter;
import com.bernardoms.user.service.UserQueryService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserQueryServiceUnitTest {
    @Mock
    private MongoOperations mongoOperations;

    @InjectMocks
    private UserQueryService userQueryService;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    @Test
    public void should_filter_by_every_field() {
        var filter = UserFilter.builder().email("test@test.com")
                .country("UK")
                .firstName("firstname")
                .lastName("lastname")
                .offset(0)
                .limit(100)
                .nickname("nick")
                .build();

        var user = User.builder().country("UK")
                .id(new ObjectId("507f191e810c19729de860ea"))
                .email("test@test.com")
                .firstName("firstname")
                .lastName("lastname")
                .nickname("nick")
                .build();


        when(mongoOperations.find(any(), any())).thenReturn(Collections.singletonList(user));

        userQueryService.returnPageableFilteredUser(filter);

        verify(mongoOperations, times(1)).find(queryArgumentCaptor.capture(), any());

        assertEquals("Query: { \"firstName\" : \"firstname\", \"lastName\" : \"lastname\", \"country\" : \"UK\", \"nickname\" : \"nick\", \"email\" : \"test@test.com\"}, Fields: {}, Sort: {}", queryArgumentCaptor.getValue().toString());
    }

    @Test
    public void should_not_filter() {
        var filter = UserFilter.builder()
                .offset(0)
                .limit(100)
                .build();

        var user = User.builder().country("UK")
                .id(new ObjectId("507f191e810c19729de860ea"))
                .email("test@test.com")
                .firstName("firstname")
                .lastName("lastname")
                .nickname("nick")
                .build();


        when(mongoOperations.find(any(), any())).thenReturn(Collections.singletonList(user));

        userQueryService.returnPageableFilteredUser(filter);

        verify(mongoOperations, times(1)).find(queryArgumentCaptor.capture(), any());

        assertEquals("Query: {}, Fields: {}, Sort: {}", queryArgumentCaptor.getValue().toString());
    }
}
