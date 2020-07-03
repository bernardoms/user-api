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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final MongoOperations mongoOperations;

    public Page<User> returnPageableFilteredUser(UserFilter userFilter) {
        var query = buildQuery(userFilter);

        var pageable = PageRequest.of(userFilter.getOffset(), userFilter.getLimit(), Sort.unsorted());

        var count = mongoOperations.count(query, User.class);

        var users = mongoOperations.find(query.with(pageable), User.class);

        return new PageImpl<>(users, pageable, count);
    }

    private Query buildQuery(UserFilter userFilter) {
        var query = new Query();

        Optional.ofNullable(userFilter.getFirstName()).ifPresent(name->query.addCriteria(Criteria.where("firstName").is(name)));

        Optional.ofNullable(userFilter.getLastName()).ifPresent(lastName->query.addCriteria(Criteria.where("lastName").is(lastName)));

        Optional.ofNullable(userFilter.getCountry()).ifPresent(country->query.addCriteria(Criteria.where("country").is(country)));

        Optional.ofNullable(userFilter.getNickname()).ifPresent(nickname->query.addCriteria(Criteria.where("nickname").is(nickname)));

        Optional.ofNullable(userFilter.getEmail()).ifPresent(email->query.addCriteria(Criteria.where("email").is(email)));

        return query;
    }
}
