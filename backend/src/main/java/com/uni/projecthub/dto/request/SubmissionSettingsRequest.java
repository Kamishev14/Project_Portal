package com.uni.projecthub.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmissionSettingsRequest {
    private LocalDateTime deadline;
}

