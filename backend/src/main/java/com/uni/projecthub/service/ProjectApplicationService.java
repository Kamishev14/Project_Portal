package com.uni.projecthub.service;

import com.uni.projecthub.dto.request.ProjectRequest;
import com.uni.projecthub.dto.request.ReviewRequest;
import com.uni.projecthub.dto.response.DashboardStatsResponse;
import com.uni.projecthub.dto.response.ProjectResponse;
import com.uni.projecthub.dto.response.UserSummaryResponse;
import com.uni.projecthub.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProjectApplicationService {

    ProjectResponse submitProject(ProjectRequest request, String studentEmail);

    List<ProjectResponse> getMyProjects(String studentEmail);

    ProjectResponse getMyProject(UUID projectId, String studentEmail);

    ProjectResponse editProject(UUID projectId, ProjectRequest request, String studentEmail);

    void deleteProject(UUID projectId, String studentEmail);

    Page<ProjectResponse> getAllProjects(ProjectStatus status, Pageable pageable);

    List<ProjectResponse> getProjectsByStudentIndex(String studentIndex);

    ProjectResponse getProjectById(UUID projectId);

    ProjectResponse reviewProject(UUID projectId, ReviewRequest request, String professorEmail);

    DashboardStatsResponse getDashboardStats();

    List<UserSummaryResponse> getProfessorOptions();
}
