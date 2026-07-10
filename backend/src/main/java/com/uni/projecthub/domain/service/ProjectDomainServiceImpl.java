package com.uni.projecthub.domain.service;

import com.uni.projecthub.entity.Project;
import com.uni.projecthub.entity.Review;
import com.uni.projecthub.entity.User;
import com.uni.projecthub.enums.ProjectStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ProjectDomainServiceImpl implements ProjectDomainService {

    // These are the only statuses a professor can SET — PENDING is never a valid decision
    private static final Set<ProjectStatus> VALID_REVIEW_OUTCOMES = Set.of(
            ProjectStatus.APPROVED,
            ProjectStatus.DECLINED,
            ProjectStatus.REVISION_REQUESTED
    );

    @Override
    public void assignQueueNumber(Project project, int nextNumber) {
        if (project.getQueueNumber() != null) {
            throw new IllegalStateException("Project already has a queue number");
        }
        project.setQueueNumber(nextNumber);
    }

    @Override
    public void validateStatusTransition(ProjectStatus current, ProjectStatus requested) {
        if (!VALID_REVIEW_OUTCOMES.contains(requested)) {
            throw new IllegalArgumentException(
                    "Invalid review decision: " + requested + ". Must be one of: " + VALID_REVIEW_OUTCOMES
            );
        }
        // Once approved or declined, cannot go back to revision
        if (current == ProjectStatus.APPROVED && requested == ProjectStatus.REVISION_REQUESTED) {
            throw new IllegalArgumentException("Cannot request revision on an already approved project");
        }
    }

    @Override
    public void assertStudentOwns(Project project, User student) {
        if (!project.getStudent().getId().equals(student.getId())) {
            throw new AccessDeniedException("You do not own this project");
        }
    }

    @Override
    public Review buildReview(Project project, User professor, ProjectStatus status, String comment) {
        return Review.builder()
                .project(project)
                .professor(professor)
                .status(status)
                .comment(comment)
                .build();
    }
}
