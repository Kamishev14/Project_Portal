package com.uni.projecthub.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubmissionSettingsResponse {
    private LocalDateTime deadline;
    private LocalDateTime updatedAt;
}

