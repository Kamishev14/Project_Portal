package com.uni.projecthub.service;

import com.uni.projecthub.dto.request.SubmissionSettingsRequest;
import com.uni.projecthub.dto.response.SubmissionSettingsResponse;

public interface SettingsService {
    SubmissionSettingsResponse getSubmissionSettings();
    SubmissionSettingsResponse updateSubmissionDeadline(SubmissionSettingsRequest request);
}

