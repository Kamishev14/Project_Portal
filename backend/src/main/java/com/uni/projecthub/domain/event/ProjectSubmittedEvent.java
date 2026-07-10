package com.uni.projecthub.domain.event;

import com.uni.projecthub.entity.Project;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Fired when a student successfully submits a new project idea.
 *
 * Using Spring's ApplicationEvent means the ProjectService doesn't need
 * to know about emails, notifications, or anything else that reacts to
 * a submission — it just fires the event and those listeners handle it.
 *
 * This is the "events + listeners" pattern your uni taught.
 */
@Getter
public class ProjectSubmittedEvent extends ApplicationEvent {

    private final Project project;

    public ProjectSubmittedEvent(Object source, Project project) {
        super(source);
        this.project = project;
    }
}
