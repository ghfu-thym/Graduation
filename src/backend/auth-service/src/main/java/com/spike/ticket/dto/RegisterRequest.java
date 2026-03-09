package com.spike.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username cannot be blank")
    @Size(max = 30, message = "Username must not exceed 30 characters")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 32, message = "Password must be between 6 and 32 characters")
    private String password;

    @NotBlank(message = "Email cannot be blank")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    private String email;
}
