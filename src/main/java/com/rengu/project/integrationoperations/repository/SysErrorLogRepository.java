package com.rengu.project.integrationoperations.repository;


import com.rengu.project.integrationoperations.entity.SysErrorLogEntity;
import com.rengu.project.integrationoperations.entity.SysLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface SysErrorLogRepository extends JpaRepository<SysErrorLogEntity,String> {




    Page<SysErrorLogEntity> findAllByCreateTimeBetween(Pageable pageable, Date st, Date ed);

    void deleteByCreateTimeBetween(Date st, Date ed);
}
