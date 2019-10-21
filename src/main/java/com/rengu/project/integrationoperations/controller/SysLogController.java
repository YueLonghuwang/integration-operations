package com.rengu.project.integrationoperations.controller;

import com.rengu.project.integrationoperations.entity.ResultEntity;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.service.SysLogService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

	// 查询日志操作时间和操作指令
	@GetMapping
	public ResultEntity getAllSysLogEntity(
			@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
		Page page = sysLogService.getAllSysLog(pageable);
		return new ResultEntity(SystemStatusCodeEnum.SUCCESS, page);
	}
	//根据选择的时间段删除操作
	@DeleteMapping
	public ResultEntity deleteByCreateTimeBeforeAndCreateTimeAfter(String startTime, String endTime) throws ParseException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date startDate = simpleDateFormat.parse(startTime);
		Date endDate = simpleDateFormat.parse(endTime);
		return new ResultEntity(SystemStatusCodeEnum.SUCCESS, sysLogService.deleteByCreateBetween(startDate,endDate));
	}
	//根据时间段查询操作
	@PatchMapping
	public ResultEntity selectByCreateBetween(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable, @Param(value = "startTime") String startTime,@Param(value = "endTime") String endTime) throws ParseException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date startDate = simpleDateFormat.parse(startTime);
		Date endDate = simpleDateFormat.parse(endTime);
		return new ResultEntity(SystemStatusCodeEnum.SUCCESS, sysLogService.selectByCreateBetween(pageable,startDate,endDate));
	}

}
