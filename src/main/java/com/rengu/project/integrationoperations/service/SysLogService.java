package com.rengu.project.integrationoperations.service;

import com.rengu.project.integrationoperations.entity.SysLogEntity;
import com.rengu.project.integrationoperations.repository.SysLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SysLogService {
    private final SysLogRepository sysLogRepository;

    @Autowired
    public SysLogService(SysLogRepository sysLogRepository) {
        this.sysLogRepository = sysLogRepository;
    }
    public SysLogEntity saveLog(SysLogEntity sysLogEntity){
        return sysLogRepository.save(sysLogEntity);
    }
    //分页查询日志执行时间及执行了哪些命令
    public Page<SysLogEntity>getAllSysLog(Pageable pageable){
        return sysLogRepository.findAll(pageable);
    }
}
