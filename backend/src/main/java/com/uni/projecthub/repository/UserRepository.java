package com.uni.projecthub.repository;

import com.uni.projecthub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByStudentIndex(String studentIndex);
    boolean existsByEmail(String email);
    boolean existsByStudentIndex(String studentIndex);
}
