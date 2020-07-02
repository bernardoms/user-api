package com.bernardoms.user.repository;

import com.bernardoms.user.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, ObjectId> {
    Optional<User> findByNickname(String nickname);

}
