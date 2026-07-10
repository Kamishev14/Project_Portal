package com.uni.projecthub.dto.response;

import com.uni.projecthub.enums.ProjectStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {
    private UUID id;
    private ProjectStatus status;
    private String comment;
    private String professorName;
    private LocalDateTime reviewedAt;
}
