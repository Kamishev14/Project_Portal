package com.uni.projecthub.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    /**
     * 6-digit university student index, e.g. "190042".
     * Required for students (@students.finki.ukim.mk).
     * Null/blank for professors (@finki.ukim.mk).
     */
    @Pattern(
        regexp = "^(\\d{6})?$",
        message = "Student index must be exactly 6 digits (e.g. 190042)"
    )
    private String studentIndex;
}
