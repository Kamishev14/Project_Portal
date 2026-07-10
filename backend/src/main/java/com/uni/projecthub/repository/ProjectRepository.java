package com.uni.projecthub.repository;

import com.uni.projecthub.entity.Project;
import com.uni.projecthub.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    // Used internally (by UUID — for the application service)
    List<Project> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    // Used by professor to look up a specific student's projects by their index
    List<Project> findByStudentStudentIndexOrderByCreatedAtDesc(String studentIndex);

    Page<Project> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Project> findByStatusOrderByCreatedAtDesc(ProjectStatus status, Pageable pageable);

    long countByStatus(ProjectStatus status);

    @Query("SELECT COALESCE(MAX(p.queueNumber), 1000) + 1 FROM Project p")
    int getNextQueueNumber();
}
