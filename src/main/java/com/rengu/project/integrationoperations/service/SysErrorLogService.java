package com.rengu.project.integrationoperations.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.rengu.project.integrationoperations.entity.SysErrorLogEntity;
import com.rengu.project.integrationoperations.repository.SysErrorLogRepository;

@Service
public class SysErrorLogService {
	private final SysErrorLogRepository sysErrorLogRepository;

	@Autowired
	public SysErrorLogService(SysErrorLogRepository sysErrorLogRepository) {
		this.sysErrorLogRepository = sysErrorLogRepository;
	}

	// 保存错误日志
	public SysErrorLogEntity saveError(SysErrorLogEntity sysLogEntity) {
		return sysErrorLogRepository.save(sysLogEntity);
	}

	// 分页查询错误日志执行时间及执行了哪些命令
	public Page getAllSysLog(Pageable pageable) {
		return sysErrorLogRepository.findAll(pageable);
	}

	// 根据选择的时间删除错误日志
	public int deleteByCreateBetween(Date startTime, Date endTime) {
		return sysErrorLogRepository.deleteByCreateTimeBetween(startTime, endTime);
	}

	// 根据时间查询日志
	public Page<SysErrorLogEntity> selectByCreateBetween(Pageable pageable, Date startTime, Date endTime) {
		return sysErrorLogRepository.findAllByCreateTimeBetween(pageable,startTime, endTime);
	}

}
