package com.rengu.project.integrationoperations.service;

import com.rengu.project.integrationoperations.entity.SysErrorLogEntity;
import com.rengu.project.integrationoperations.repository.SysErrorLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/10/8 13:47
 */
@Service
public class SysErrorLogService {
    private final SysErrorLogRepository sysErrorLogRepository;

    public SysErrorLogService(SysErrorLogRepository sysErrorLogRepository) {
        this.sysErrorLogRepository = sysErrorLogRepository;
    }

    public SysErrorLogEntity getError(String sysErrorLogEntityId) {
        return sysErrorLogRepository.findById(sysErrorLogEntityId).get();
    }

    public SysErrorLogEntity saveError(SysErrorLogEntity sysErrorLogEntity) {
        return sysErrorLogRepository.save(sysErrorLogEntity);
    }


    public void deleteById(String id){
         sysErrorLogRepository.deleteById(id);
    }

    public void deleteByTime(String st, String ed){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            sysErrorLogRepository.deleteByCreateTimeBetween(sdf.parse(st),sdf.parse(ed));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }



    public Page<SysErrorLogEntity> findErrorByPage(Pageable pageable, String st, String ed) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (st != null && ed != null) {
            try {
                return sysErrorLogRepository.findAllByCreateTimeBetween(pageable,sdf.parse(st),sdf.parse(ed));
            } catch (ParseException e) {
                return null;
            }
        } else {
            return sysErrorLogRepository.findAll(pageable);
        }
    }


}
