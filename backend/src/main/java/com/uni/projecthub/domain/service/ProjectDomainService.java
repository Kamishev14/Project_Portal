package com.uni.projecthub.domain.service;

import com.uni.projecthub.entity.Project;
import com.uni.projecthub.entity.Review;
import com.uni.projecthub.entity.User;
import com.uni.projecthub.enums.ProjectStatus;

/**
 * Domain Service — pure business rules with no infrastructure concerns.
 * No database calls, no emails, no HTTP here — just logic.
 */
public interface ProjectDomainService {

    /**
     * Assigns the next queue number to a newly created project.
     */
    void assignQueueNumber(Project project, int nextNumber);

    /**
     * Validates that a status transition is legal.
     * e.g. you cannot transition from APPROVED back to PENDING.
     */
    void validateStatusTransition(ProjectStatus current, ProjectStatus requested);

    /**
     * Verifies that a student owns the project they're trying to access.
     */
    void assertStudentOwns(Project project, User student);

    /**
     * Builds a Review entity from the given inputs without persisting it.
     */
    Review buildReview(Project project, User professor, ProjectStatus status, String comment);
}
