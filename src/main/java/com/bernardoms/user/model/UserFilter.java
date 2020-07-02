package com.bernardoms.user.model;

import com.bernardoms.user.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Getter
@Setter
@SuperBuilder
public class UserFilter extends UserDTO {
    private Integer offset;
    private Integer limit;

    public UserFilter(String nickname, String firstName, String lastName, String email,  String country) {
        super(nickname, firstName, lastName, email, country);
        this.limit = Objects.isNull(this.limit)  ? 100 : limit;
        this.offset = Objects.isNull(this.offset) ? 0 : offset;
    }
}
