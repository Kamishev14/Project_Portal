package com.uni.projecthub.repository;

import com.uni.projecthub.entity.User;
import com.uni.projecthub.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByStudentIndex(String studentIndex);
    Optional<User> findByIdAndRole(UUID id, Role role);
    List<User> findAllByRoleOrderByNameAsc(Role role);
    List<User> findAllByStudentIndexIn(Collection<String> studentIndexes);
    boolean existsByEmail(String email);
    boolean existsByStudentIndex(String studentIndex);
}
