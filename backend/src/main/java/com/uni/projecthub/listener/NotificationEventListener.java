package com.uni.projecthub.listener;

import com.uni.projecthub.domain.event.ProjectReviewedEvent;
import com.uni.projecthub.domain.event.ProjectSubmittedEvent;
import com.uni.projecthub.entity.Project;
import com.uni.projecthub.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

/**
 * Event Listener — this is what your uni was teaching you about.
 *
 * How it works:
 *   1. ProjectApplicationService publishes a ProjectSubmittedEvent
 *   2. Spring sees @EventListener and automatically calls these methods
 *   3. The service that fired the event doesn't know or care about this class
 *
 * Why it's useful:
 *   - ProjectApplicationService only handles project logic
 *   - Emails are a separate concern handled here
 *   - To add Slack notifications later, you just add another listener —
 *     you don't touch ProjectApplicationService at all
 *
 * Async means these run in a background thread so the HTTP response
 * returns immediately without waiting for the email to send.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onProjectSubmitted(ProjectSubmittedEvent event) {

        log.info("Event received: ProjectSubmitted [projectId={}]",
                event.getProject().getId());
        Project p = event.getProject();

        String studentName = p.getStudent().getName();
        String studentEmail = p.getStudent().getEmail();
        String title = p.getTitle();
        String queue = String.valueOf(p.getQueueNumber());
        UUID id = p.getId();

        emailService.sendSubmissionConfirmation(
                studentName,
                studentEmail,
                title,
                queue,
                id
        );        emailService.sendNewSubmissionAlert(event.getProject());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onProjectReviewed(ProjectReviewedEvent event) {

        log.info("Event received: ProjectReviewed [projectId={}, status={}]",
                event.getProject().getId(),
                event.getReview().getStatus());

        emailService.sendReviewNotification(event.getProject(), event.getReview());
    }
}
