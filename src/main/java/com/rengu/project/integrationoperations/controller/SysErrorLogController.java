package com.rengu.project.integrationoperations.controller;

import org.springframework.data.domain.Pageable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rengu.project.integrationoperations.entity.ResultEntity;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.service.SysErrorLogService;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;

/**
 * @author yyc
 * @Date 2019/10/8 16:45
 */
@RequestMapping(path = "/errorLog")
@RestController

public class SysErrorLogController {
	private final SysErrorLogService sysErrorLogService;

	@Autowired
	public SysErrorLogController(SysErrorLogService sysErrorLogService) {
		this.sysErrorLogService = sysErrorLogService;
	}

	// 查询错误日志
	@GetMapping
	public ResultEntity getAllSysLogEntity(
			@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
		return new ResultEntity(SystemStatusCodeEnum.SUCCESS, sysErrorLogService.getAllSysLog(pageable));
	}

	// 根据选择的时间段删除错误日志
	@DeleteMapping
	public ResultEntity deleteByCreateTimeBeforeAndCreateTimeAfter(String startTime, String endTime)
			throws ParseException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date startDate = simpleDateFormat.parse(startTime);
		Date endDate = simpleDateFormat.parse(endTime);
		return new ResultEntity(SystemStatusCodeEnum.SUCCESS,
				sysErrorLogService.deleteByCreateBetween(startDate, endDate));
	}

	// 根据时间段查询错误日志
	@PatchMapping
	public ResultEntity selectByCreateBetween(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable,@Param(value = "startTime") String startTime,
											  @Param(value = "endTime") String endTime) throws ParseException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate = simpleDateFormat.parse(startTime);
		Date endDate = simpleDateFormat.parse(endTime);
		return new ResultEntity(SystemStatusCodeEnum.SUCCESS,
				sysErrorLogService.selectByCreateBetween(pageable,startDate, endDate));
	}

}
