package com.uni.projecthub.repository;

import com.uni.projecthub.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByProjectIdOrderByReviewedAtDesc(UUID projectId);
}
