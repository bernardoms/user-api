package com.bernardoms.user.service;

import com.bernardoms.user.model.User;
import com.bernardoms.user.model.UserFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final MongoOperations mongoOperations;

    public Page<User> returnPageableFilteredUser(UserFilter userFilter) {
        Query query = new Query();

        Optional.ofNullable(userFilter.getFirstName()).ifPresent(c->query.addCriteria(Criteria.where("firstName").is(userFilter.getFirstName())));

        Optional.ofNullable(userFilter.getLastName()).ifPresent(c->query.addCriteria(Criteria.where("lastName").is(userFilter.getLastName())));

        Optional.ofNullable(userFilter.getCountry()).ifPresent(c->query.addCriteria(Criteria.where("country").is(userFilter.getCountry())));

        Optional.ofNullable(userFilter.getNickname()).ifPresent(c->query.addCriteria(Criteria.where("nickname").is(userFilter.getNickname())));

        Optional.ofNullable(userFilter.getEmail()).ifPresent(c->query.addCriteria(Criteria.where("email").is(userFilter.getEmail())));

        var pageable = PageRequest.of(userFilter.getOffset(), userFilter.getLimit(), Sort.unsorted());

        long count = mongoOperations.count(query, User.class);

        List<User> users = mongoOperations.find(query.with(pageable), User.class);

        return new PageImpl<>(users, pageable, count);
    }
}
