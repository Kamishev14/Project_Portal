package com.uni.projecthub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ProjectRequest {
    @NotBlank
    @Size(min = 5, max = 200)
    private String title;

    @NotBlank
    @Size(min = 20)
    private String description;

    private UUID preferredProfessorId;

    private List<@Pattern(
            regexp = "^\\d{6}$",
            message = "Each participant index must be exactly 6 digits"
    ) String> participantStudentIndexes;
}
