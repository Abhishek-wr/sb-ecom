package com.ecommerce.project.security.response;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

public class UserInfoResponse {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String jwtToken;
    private List<String> roles;

    public UserInfoResponse(Long id, String username, List<String> roles) {
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    public UserInfoResponse(Long id, String username, String jwtToken, List<String> roles) {
        this.id = id;
        this.username = username;
        this.jwtToken = jwtToken;
        this.roles = roles;
    }
}
