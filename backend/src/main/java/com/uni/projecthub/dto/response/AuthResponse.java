package com.uni.projecthub.dto.response;

import com.uni.projecthub.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String name;
    private String email;
    private Role role;
    private UUID userId;
    private String studentIndex;   // null for professors
}
