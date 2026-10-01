package com.uni.projecthub.controller;

import com.uni.projecthub.dto.request.ProjectRequest;
import com.uni.projecthub.dto.request.ReviewRequest;
import com.uni.projecthub.dto.response.DashboardStatsResponse;
import com.uni.projecthub.dto.response.ProjectResponse;
import com.uni.projecthub.dto.response.UserSummaryResponse;
import com.uni.projecthub.enums.ProjectStatus;
import com.uni.projecthub.service.ProjectApplicationService;
import com.uni.projecthub.util.QrCodeUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Validated
public class


ProjectController {

    private final ProjectApplicationService projectService;
    private final QrCodeUtil qrCodeUtil;

    // ── Student endpoints ──────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ProjectResponse> submit(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(projectService.submitProject(request, user.getUsername()));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ProjectResponse>> getMyProjects(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(projectService.getMyProjects(user.getUsername()));
    }

    @GetMapping("/mine/{id}")
    public ResponseEntity<ProjectResponse> getMyProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(projectService.getMyProject(id, user.getUsername()));
    }

    /** Edit own project — only allowed if status is PENDING or REVISION_REQUESTED */
    @PutMapping("/mine/{id}")
    public ResponseEntity<ProjectResponse> editMyProject(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(projectService.editProject(id, request, user.getUsername()));
    }

    /** Delete own project — only allowed if status is PENDING or REVISION_REQUESTED */
    @DeleteMapping("/mine/{id}")
    public ResponseEntity<Void> deleteMyProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails user) {
        projectService.deleteProject(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ── Professor endpoints ────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> getAllProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) String title,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(projectService.getAllProjects(status, title, pageable));
    }

    @GetMapping("/student/{studentIndex}")
    public ResponseEntity<List<ProjectResponse>> getByStudentIndex(
            @PathVariable
            @Pattern(regexp = "^\\d{6}$", message = "Student index must be exactly 6 digits")
            String studentIndex) {
        return ResponseEntity.ok(projectService.getProjectsByStudentIndex(studentIndex));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<ProjectResponse> review(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(projectService.reviewProject(id, request, user.getUsername()));
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(projectService.getDashboardStats());
    }

    @GetMapping("/professors/options")
    public ResponseEntity<List<UserSummaryResponse>> getProfessorOptions() {
        return ResponseEntity.ok(projectService.getProfessorOptions());
    }

    // ── Shared ────────────────────────────────────────────────────────────────

    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode(@PathVariable UUID id) throws Exception {
        ProjectResponse project = projectService.getProjectById(id);
        byte[] qr = qrCodeUtil.generateQrPng(project.getQrqVisitorUrl(), 300);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(qr);
    }
}
