package com.firstclub.membership.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    private String cohort; // Optional: e.g., "STUDENT", "VIP"
}
