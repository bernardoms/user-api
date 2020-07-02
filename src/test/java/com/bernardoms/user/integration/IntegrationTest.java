package com.bernardoms.user.integration;

import com.bernardoms.user.model.User;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootTest
public abstract class IntegrationTest {


    private static boolean alreadySaved = false;

    @Autowired
    MongoTemplate mongoTemplate;


    @BeforeEach
    public void setUp() {

        if (alreadySaved) {
            return;
        }
        mongoTemplate.save(User.builder().id(new ObjectId("507f191e810c19729de860ea")).country("UK").email("test@test.com").firstName("test").lastName("testing").nickname("test_nick").password("test_password").build());
        mongoTemplate.save(User.builder().id(new ObjectId("507f191e810c19729de860eb")).country("BR").email("test2@test.com").firstName("test2").lastName("testing2").nickname("test_nick2").password("test_password2").build());
        mongoTemplate.save(User.builder().id(new ObjectId("507f191e810c19729de860ec")).country("BR").email("test3@test.com").firstName("test2").lastName("testing2").nickname("test_nick3").password("test_password3").build());
        alreadySaved = true;
    }
}