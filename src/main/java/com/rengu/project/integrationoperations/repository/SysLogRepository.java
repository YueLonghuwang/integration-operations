package com.rengu.project.integrationoperations.repository;

import com.rengu.project.integrationoperations.entity.SysLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysLogRepository extends JpaRepository<SysLogEntity,String> {
    Page<SysLogEntity> findAll(Pageable pageable);

    SysLogEntity deleteByCreateTime(String start,String end);
}
