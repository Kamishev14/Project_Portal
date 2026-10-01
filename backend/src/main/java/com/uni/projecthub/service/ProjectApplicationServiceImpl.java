package com.uni.projecthub.service;

import com.uni.projecthub.domain.event.ProjectReviewedEvent;
import com.uni.projecthub.domain.event.ProjectSubmittedEvent;
import com.uni.projecthub.domain.service.ProjectDomainService;
import com.uni.projecthub.dto.request.ProjectRequest;
import com.uni.projecthub.dto.request.ReviewRequest;
import com.uni.projecthub.dto.response.DashboardStatsResponse;
import com.uni.projecthub.dto.response.ProjectResponse;
import com.uni.projecthub.dto.response.ReviewResponse;
import com.uni.projecthub.dto.response.UserSummaryResponse;
import com.uni.projecthub.entity.Project;
import com.uni.projecthub.entity.Review;
import com.uni.projecthub.entity.User;
import com.uni.projecthub.enums.ProjectStatus;
import com.uni.projecthub.enums.Role;
import com.uni.projecthub.exception.ResourceNotFoundException;
import com.uni.projecthub.repository.ProjectRepository;
import com.uni.projecthub.repository.ReviewRepository;
import com.uni.projecthub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectApplicationServiceImpl implements ProjectApplicationService {

    private final ProjectRepository projectRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProjectDomainService domainService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.queue.qrq-visitor-url}")
    private String qrqVisitorUrl;   // goes inside the QR code

    @Value("${app.queue.qrq-status-url}")
    private String qrqStatusUrl;    // display board

    // ── Student: submit ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProjectResponse submitProject(ProjectRequest request, String studentEmail) {
        User student = findUserByEmail(studentEmail);

        Project project = Project.builder()
                .status(ProjectStatus.PENDING)
                .student(student)
                .build();

        applyProjectDetails(project, request, student);

        int nextNumber = projectRepository.getNextQueueNumber();
        domainService.assignQueueNumber(project, nextNumber);

        projectRepository.save(project);
        eventPublisher.publishEvent(new ProjectSubmittedEvent(this, project));

        return toResponse(project);
    }

    // ── Student: my projects ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getMyProjects(String studentEmail) {
        User student = findUserByEmail(studentEmail);
        return projectRepository
                .findByStudentIdOrderByCreatedAtDesc(student.getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getMyProject(UUID projectId, String studentEmail) {
        Project project = findProject(projectId);
        User student = findUserByEmail(studentEmail);
        domainService.assertStudentOwns(project, student);
        return toResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse editProject(UUID projectId, ProjectRequest request, String studentEmail) {
        Project project = findProject(projectId);
        User student = findUserByEmail(studentEmail);
        domainService.assertStudentOwns(project, student);

        // Only editable if pending or revision requested — not after approval/decline
        if (project.getStatus() == ProjectStatus.APPROVED ||
                project.getStatus() == ProjectStatus.DECLINED) {
            throw new IllegalArgumentException(
                    "Cannot edit a project that has been " + project.getStatus().name().toLowerCase()
            );
        }

        applyProjectDetails(project, request, student);
        projectRepository.save(project);

        return toResponse(project);
    }

    @Override
    @Transactional
    public void deleteProject(UUID projectId, String studentEmail) {
        Project project = findProject(projectId);
        User student = findUserByEmail(studentEmail);
        domainService.assertStudentOwns(project, student);

        if (project.getStatus() == ProjectStatus.APPROVED ||
                project.getStatus() == ProjectStatus.DECLINED) {
            throw new IllegalArgumentException(
                    "Cannot delete a project that has been " + project.getStatus().name().toLowerCase()
            );
        }

        projectRepository.delete(project);
    }

    // ── Professor: all projects ───────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getAllProjects(ProjectStatus status, String title, Pageable pageable) {
        String normalizedTitle = (title == null) ? null : title.trim();

        if (status != null && normalizedTitle != null && !normalizedTitle.isBlank()) {
            return projectRepository
                    .findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(status, normalizedTitle, pageable)
                    .map(this::toResponse);
        }

        if (status != null) {
            return projectRepository
                    .findByStatusOrderByCreatedAtDesc(status, pageable)
                    .map(this::toResponse);
        }

        if (normalizedTitle != null && !normalizedTitle.isBlank()) {
            return projectRepository
                    .findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(normalizedTitle, pageable)
                    .map(this::toResponse);
        }

        return projectRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    // ── Professor: projects by student index ──────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByStudentIndex(String studentIndex) {
        if (!userRepository.existsByStudentIndex(studentIndex)) {
            throw new ResourceNotFoundException("No student found with index: " + studentIndex);
        }
        return projectRepository
                .findByStudentStudentIndexOrderByCreatedAtDesc(studentIndex)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID projectId) {
        return toResponse(findProject(projectId));
    }

    // ── Professor: review ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProjectResponse reviewProject(UUID projectId, ReviewRequest request, String professorEmail) {
        Project project = findProject(projectId);
        User professor = findUserByEmail(professorEmail);

        domainService.validateStatusTransition(project.getStatus(), request.getStatus());

        Review review = domainService.buildReview(
                project, professor, request.getStatus(), request.getComment());

        project.setStatus(request.getStatus());
        projectRepository.save(project);
        reviewRepository.save(review);

        eventPublisher.publishEvent(new ProjectReviewedEvent(this, project, review));

        return toResponse(project);
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        return DashboardStatsResponse.builder()
                .totalProjects(projectRepository.count())
                .pending(projectRepository.countByStatus(ProjectStatus.PENDING))
                .approved(projectRepository.countByStatus(ProjectStatus.APPROVED))
                .declined(projectRepository.countByStatus(ProjectStatus.DECLINED))
                .revisionRequested(projectRepository.countByStatus(ProjectStatus.REVISION_REQUESTED))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getProfessorOptions() {
        return userRepository.findAllByRoleOrderByNameAsc(Role.PROFESSOR)
                .stream()
                .map(this::toUserSummary)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void applyProjectDetails(Project project, ProjectRequest request, User owner) {
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setPreferredProfessor(resolvePreferredProfessor(request.getPreferredProfessorId()));
        Set<User> resolvedParticipants = resolveParticipants(request.getParticipantStudentIndexes(), owner);
        project.getParticipants().clear();
        project.getParticipants().addAll(resolvedParticipants);
    }

    private Project findProject(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private User resolvePreferredProfessor(UUID preferredProfessorId) {
        if (preferredProfessorId == null) {
            return null;
        }

        return userRepository.findByIdAndRole(preferredProfessorId, Role.PROFESSOR)
                .orElseThrow(() -> new ResourceNotFoundException("Professor not found: " + preferredProfessorId));
    }

    private Set<User> resolveParticipants(List<String> participantStudentIndexes, User owner) {
        if (participantStudentIndexes == null || participantStudentIndexes.isEmpty()) {
            return new LinkedHashSet<>();
        }

        List<String> normalizedIndexes = participantStudentIndexes.stream()
                .filter(index -> index != null && !index.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        if (normalizedIndexes.isEmpty()) {
            return new LinkedHashSet<>();
        }

        if (owner.getStudentIndex() != null && normalizedIndexes.contains(owner.getStudentIndex())) {
            throw new IllegalArgumentException("Do not include your own student index as a participant");
        }

        List<User> participants = userRepository.findAllByStudentIndexIn(normalizedIndexes);
        Map<String, User> usersByIndex = participants.stream()
                .collect(Collectors.toMap(User::getStudentIndex, user -> user, (left, right) -> left, LinkedHashMap::new));

        List<String> missingIndexes = normalizedIndexes.stream()
                .filter(index -> !usersByIndex.containsKey(index))
                .toList();

        if (!missingIndexes.isEmpty()) {
            throw new ResourceNotFoundException("Participant(s) not found: " + String.join(", ", missingIndexes));
        }

        boolean hasNonStudentParticipant = participants.stream()
                .anyMatch(user -> user.getRole() != Role.STUDENT);

        if (hasNonStudentParticipant) {
            throw new IllegalArgumentException("Only student accounts can be added as participants");
        }

        Set<User> orderedParticipants = new LinkedHashSet<>();
        normalizedIndexes.forEach(index -> orderedParticipants.add(usersByIndex.get(index)));
        return orderedParticipants;
    }

    private UserSummaryResponse toUserSummary(User user) {
        if (user == null) {
            return null;
        }

        return UserSummaryResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .studentIndex(user.getStudentIndex())
                .build();
    }

    private ProjectResponse toResponse(Project p) {
        List<ReviewResponse> reviews = p.getReviews().stream()
                .map(r -> ReviewResponse.builder()
                        .id(r.getId())
                        .status(r.getStatus())
                        .comment(r.getComment())
                        .professorName(r.getProfessor().getName())
                        .reviewedAt(r.getReviewedAt())
                        .build())
                .toList();

        return ProjectResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .status(p.getStatus())
                .queueNumber(p.getQueueNumber())
                .qrCodeUrl(frontendUrl + "/api/projects/" + p.getId() + "/qr")
                .qrqVisitorUrl(qrqVisitorUrl)   // inside the QR — student scans this
                .qrqStatusUrl(qrqStatusUrl)      // display board link
                .student(toUserSummary(p.getStudent()))
                .preferredProfessor(toUserSummary(p.getPreferredProfessor()))
                .participants(p.getParticipants().stream().map(this::toUserSummary).toList())
                .reviews(reviews)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }


}
