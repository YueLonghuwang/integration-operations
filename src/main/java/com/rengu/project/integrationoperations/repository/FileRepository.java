package com.rengu.project.integrationoperations.repository;


import com.rengu.project.integrationoperations.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 10:17
 **/

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {

    boolean existsByMD5(String md5);

    Optional<FileEntity> findByMD5(String md5);
}
