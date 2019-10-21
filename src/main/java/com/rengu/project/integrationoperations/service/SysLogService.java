package com.rengu.project.integrationoperations.service;

import com.rengu.project.integrationoperations.entity.SysLogEntity;
import com.rengu.project.integrationoperations.repository.SysLogRepository;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;

@Service
public class SysLogService {
	private final SysLogRepository sysLogRepository;

	@Autowired
	public SysLogService(SysLogRepository sysLogRepository) {
		this.sysLogRepository = sysLogRepository;
	}

	public SysLogEntity saveLog(SysLogEntity sysLogEntity) {
		return sysLogRepository.save(sysLogEntity);
	}

	// 分页查询日志执行时间及执行了哪些命令
	public Page getAllSysLog(Pageable pageable) {
		return sysLogRepository.findAll(pageable);
	}

	// 根据选择的时间删除操作日志
	public int deleteByCreateBetween(Date startTime, Date endTime) {
		return sysLogRepository.deleteByCreateTimeBetween(startTime, endTime);
	}

	// 根据时间查询日志
	public Page<SysLogEntity> selectByCreateBetween(Pageable pageable, Date startTime, Date endTime) {
		if (startTime != null && endTime != null) {
			return sysLogRepository.findAllByCreateTimeBetween(pageable,startTime, endTime);
		} else {
			return sysLogRepository.findAll(pageable);
		}
	}

}
