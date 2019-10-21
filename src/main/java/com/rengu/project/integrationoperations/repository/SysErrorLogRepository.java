package com.rengu.project.integrationoperations.repository;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rengu.project.integrationoperations.entity.SysErrorLogEntity;

@Repository
public interface SysErrorLogRepository extends JpaRepository<SysErrorLogEntity, String> {

	Page<SysErrorLogEntity> findAllByCreateTimeBetween(Pageable pageable, Date st, Date ed);

	@Transactional
	int deleteByCreateTimeBetween(Date st, Date ed);


}
