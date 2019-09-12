package com.rengu.project.integrationoperations.controller;

import com.rengu.project.integrationoperations.entity.ResultEntity;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yyc
 * @Date 2019/9/12 10:45
 */
@RequestMapping(path = "/log")
@RestController
public class SysLogController {

    private final SysLogService sysLogService;
    @Autowired
    public SysLogController(SysLogService sysLogService) {
        this.sysLogService = sysLogService;
    }
    //查询日志操作时间和操作指令
    @GetMapping
    public ResultEntity getAllSysLogEntity(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable){
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS,sysLogService.getAllSysLog(pageable));
    }
}
