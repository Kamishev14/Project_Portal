package com.uni.projecthub.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {
    private long totalProjects;
    private long pending;
    private long approved;
    private long declined;
    private long revisionRequested;
}
