package com.rengu.project.integrationoperations.repository;

import com.rengu.project.integrationoperations.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 *
 * @author hanchangming
 * @date 2019-03-19
 */

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, String> {

    boolean existsByName(String name);

    Optional<RoleEntity> findByName(String name);
}
