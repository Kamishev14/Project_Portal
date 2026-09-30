package com.uni.projecthub.dto.response;

import com.uni.projecthub.enums.ProjectStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProjectResponse {
    private UUID id;
    private String title;
    private String description;
    private ProjectStatus status;
    private Integer queueNumber;
    private String qrCodeUrl;       // endpoint that returns the QR image PNG
    private String qrqVisitorUrl;   // goes INSIDE the QR — student scans to join queue
    private String qrqStatusUrl;    // display board link — show on TV/screen
    private UserSummaryResponse student;
    private UserSummaryResponse preferredProfessor;
    private List<UserSummaryResponse> participants;
    private List<ReviewResponse> reviews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
