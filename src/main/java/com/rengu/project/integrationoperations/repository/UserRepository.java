package com.rengu.project.integrationoperations.repository;

import com.rengu.project.integrationoperations.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 *
 * @author hanchangming
 * @date 2019-03-19
 */

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    boolean existsByUsername(String username);

    boolean existsById(String userId);
    Optional<UserEntity> findByUsername(String username);
}