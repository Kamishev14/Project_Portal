package com.uni.projecthub.controller;

import com.uni.projecthub.dto.request.SubmissionSettingsRequest;
import com.uni.projecthub.dto.response.SubmissionSettingsResponse;
import com.uni.projecthub.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/submission-deadline")
    public ResponseEntity<SubmissionSettingsResponse> getSubmissionDeadline() {
        return ResponseEntity.ok(settingsService.getSubmissionSettings());
    }

    @PutMapping("/submission-deadline")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<SubmissionSettingsResponse> updateSubmissionDeadline(
            @Valid @RequestBody SubmissionSettingsRequest request) {
        return ResponseEntity.ok(settingsService.updateSubmissionDeadline(request));
    }
}

