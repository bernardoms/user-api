package com.bernardoms.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private ObjectId id;
    @Indexed(name = "nickname", unique = true)
    private String nickname;
    private String firstName;
    private String lastName;
    private String password;
    @Indexed(name = "email", unique = true)
    private String email;
    private String country;
}

