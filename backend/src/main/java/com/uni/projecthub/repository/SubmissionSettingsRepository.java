package com.uni.projecthub.repository;

import com.uni.projecthub.entity.SubmissionSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SubmissionSettingsRepository extends JpaRepository<SubmissionSettings, UUID> {
}

