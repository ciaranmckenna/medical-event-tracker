package com.ciaranmckenna.medical_event_tracker.repository;

import com.ciaranmckenna.medical_event_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.username = :username OR u.email = :email")
    Optional<User> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE (u.username = :username OR u.email = :email) AND u.id != :id")
    boolean existsByUsernameOrEmailAndNotId(@Param("username") String username, @Param("email") String email, @Param("id") UUID id);
}