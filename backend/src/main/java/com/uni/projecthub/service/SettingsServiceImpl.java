package com.uni.projecthub.service;

import com.uni.projecthub.dto.request.SubmissionSettingsRequest;
import com.uni.projecthub.dto.response.SubmissionSettingsResponse;
import com.uni.projecthub.entity.SubmissionSettings;
import com.uni.projecthub.repository.SubmissionSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final SubmissionSettingsRepository settingsRepository;

    @Override
    @Transactional(readOnly = true)
    public SubmissionSettingsResponse getSubmissionSettings() {
        SubmissionSettings settings = getOrCreateSettings();
        return toResponse(settings);
    }

    @Override
    @Transactional
    public SubmissionSettingsResponse updateSubmissionDeadline(SubmissionSettingsRequest request) {
        SubmissionSettings settings = getOrCreateSettings();
        settings.setDeadline(request.getDeadline());
        settings.setUpdatedAt(LocalDateTime.now());
        settingsRepository.save(settings);
        return toResponse(settings);
    }

    private SubmissionSettings getOrCreateSettings() {
        return settingsRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    SubmissionSettings newSettings = SubmissionSettings.builder()
                            .deadline(null)
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return settingsRepository.save(newSettings);
                });
    }

    private SubmissionSettingsResponse toResponse(SubmissionSettings settings) {
        return SubmissionSettingsResponse.builder()
                .deadline(settings.getDeadline())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}

