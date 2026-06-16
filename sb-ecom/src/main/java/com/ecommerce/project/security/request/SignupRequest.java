package com.ecommerce.project.security.request;

import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
@Data
//@Table(name = "SignupRequest",uniqueConstraints = {
//        @UniqueConstraint(columnNames = "username"),
//        @UniqueConstraint(columnNames =  "email")
//})
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;
    @Email
    @Size(max = 50)
    private String email;

    private Set<String> role;
    @NotBlank
    @Size(min = 8, max = 50)
    private String password;
}
