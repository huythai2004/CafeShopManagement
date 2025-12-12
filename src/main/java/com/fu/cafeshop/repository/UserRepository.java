package com.fu.cafeshop.repository;

import com.fu.cafeshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRole(String role);

    List<User> findByIsActiveTrue();

    @Query("SELECT u FROM User u WHERE u.role = 'STAFF' AND u.isActive = true")
    List<User> findActiveStaff();

    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.isActive = true")
    List<User> findActiveAdmins();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = ?1")
    long countByRole(String role);
}

