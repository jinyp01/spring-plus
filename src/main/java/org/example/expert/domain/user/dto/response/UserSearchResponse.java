package org.example.expert.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserSearchResponse {

    private final Long id;
    private final String email;
    private final String username;

    public UserSearchResponse(Long id, String email, String username) {
        this.id = id;
        this.email = email;
        this.username = username;
    }
}
