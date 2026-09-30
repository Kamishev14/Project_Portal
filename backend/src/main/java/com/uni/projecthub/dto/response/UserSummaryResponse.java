package com.uni.projecthub.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserSummaryResponse {
    private UUID id;
    private String name;
    private String email;
    private String studentIndex;
}

