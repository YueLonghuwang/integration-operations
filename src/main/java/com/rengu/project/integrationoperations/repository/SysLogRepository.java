package com.rengu.project.integrationoperations.repository;

import com.rengu.project.integrationoperations.entity.SysLogEntity;

import java.util.Date;
import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysLogRepository extends JpaRepository<SysLogEntity, String> {
	@Transactional
	int deleteByCreateTimeBetween(Date createTime, Date createTime2);

	Page<SysLogEntity>findAllByCreateTimeBetween(Pageable pageable,Date createTime, Date createTime2);
}
