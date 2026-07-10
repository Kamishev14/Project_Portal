package com.uni.projecthub.domain.event;

import com.uni.projecthub.entity.Project;
import com.uni.projecthub.entity.Review;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Fired when a professor submits a review decision on a project.
 */
@Getter
public class ProjectReviewedEvent extends ApplicationEvent {

    private final Project project;
    private final Review  review;

    public ProjectReviewedEvent(Object source, Project project, Review review) {
        super(source);
        this.project = project;
        this.review  = review;
    }
}
