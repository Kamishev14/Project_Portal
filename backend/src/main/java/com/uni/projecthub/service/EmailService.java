package com.uni.projecthub.service;

import com.uni.projecthub.entity.Project;
import com.uni.projecthub.entity.Review;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final String fromEmail = "projecthub@local.test";

    @Value("${app.queue.qrq-visitor-url}")
    private String qrqVisitorUrl;

    @Value("${app.queue.qrq-status-url}")
    private String qrqStatusUrl;

    @Async
    public void sendSubmissionConfirmation(
            String studentName,
            String studentEmail,
            String title,
            String queueNumber,
            UUID projectId
    ){

        String subject = "Project Submitted – Queue #" + queueNumber;

        String body = """
            Hi %s,

            Your project idea has been successfully submitted!

            Title: %s
            Queue Number: #%s
            Status: PENDING

            You can track your position in the review queue here:
            %s

            View your project in the portal:
            %s/student/projects/%s

            We'll notify you by email when the professor reviews your submission.

            Good luck!
            – ProjectHub
            """.formatted(
                studentEmail,
                title,
                queueNumber,
                queueNumber != null ? qrqVisitorUrl : frontendUrl,
                frontendUrl,
                projectId
        );

        send(studentEmail, subject, body);
    }

    @Async
    public void sendReviewNotification(Project project, Review review) {
        String statusLabel = switch (review.getStatus()) {
            case APPROVED            -> "✅ Approved";
            case DECLINED            -> "❌ Declined";
            case REVISION_REQUESTED  -> "🔄 Revision Requested";
            default                  -> review.getStatus().name();
        };

        String subject = "Project Update: " + statusLabel + " – " + project.getTitle();
        String body = """
                Hi %s,

                Your project "%s" has been reviewed.

                Decision: %s
                %s

                View the full details here:
                %s/student/projects/%s

                %s
                – ProjectHub
                """.formatted(
                project.getStudent().getName(),
                project.getTitle(),
                statusLabel,
                review.getComment() != null
                        ? "Professor's comment:\n" + review.getComment()
                        : "",
                frontendUrl,
                project.getId(),
                review.getStatus().name().equals("REVISION_REQUESTED")
                        ? "Please update your submission and resubmit for review."
                        : ""
        );

        send(project.getStudent().getEmail(), subject, body);
    }

    @Async
    public void sendNewSubmissionAlert(Project project) {
        // Notify professor(s) that a new project is pending
        String subject = "New Project Submission – Queue #" + project.getQueueNumber();
        String body = """
                A new project idea has been submitted and is awaiting your review.

                Student: %s (%s)
                Title: %s
                Queue #: %d

                Review it here:
                %s/professor/projects/%s
                """.formatted(
                project.getStudent().getName(),
                project.getStudent().getEmail(),
                project.getTitle(),
                project.getQueueNumber(),
                frontendUrl,
                project.getId()
        );

        // In a real system you'd query all professors; for now use a config value
        // This is intentionally simple — extend as needed
        log.info("New project submission alert prepared for project {}", project.getId());
        // send("professor@uni.edu", subject, body); // uncomment and configure
    }


    private void send(String to, String subject, String text) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail.trim());
            msg.setTo(to.trim());
            msg.setSubject(subject);
            msg.setText(text);

            mailSender.send(msg);

            log.info("Email sent to {}: {}", to, subject);

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }
}
