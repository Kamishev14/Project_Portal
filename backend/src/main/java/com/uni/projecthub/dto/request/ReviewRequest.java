package com.uni.projecthub.dto.request;

import com.uni.projecthub.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotNull
    private ProjectStatus status; // APPROVED, DECLINED, or REVISION_REQUESTED

    private String comment;
}
