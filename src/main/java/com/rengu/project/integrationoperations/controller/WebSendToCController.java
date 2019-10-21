package com.rengu.project.integrationoperations.controller;

import com.rengu.project.integrationoperations.configuration.LogConfig;
//import com.rengu.project.integrationoperations.configuration.LogConfig;
import com.rengu.project.integrationoperations.entity.*;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.service.WebSendToCService;
import com.rengu.project.integrationoperations.service.SysErrorLogService;
import com.rengu.project.integrationoperations.service.WebReceiveToCService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * web端发送到c++端
 *
 * @Author: yaojiahao
 * @Date: 2019/4/12 13:30
 */
@RestController
@RequestMapping(path = "/deployment")
@Slf4j
public class WebSendToCController {
	private final WebSendToCService webSendToCService;
	private final WebReceiveToCService receiveInformationService;
	private final SysErrorLogService sysErrorLogService;

	// 设置同批数据为同一序号
	private final int serialNumber = 0;

	@Autowired
	public WebSendToCController(WebSendToCService webSendToCService, WebReceiveToCService receiveInformationService,
								SysErrorLogService sysErrorLogService) {
		this.webSendToCService = webSendToCService;
		this.receiveInformationService = receiveInformationService;
		this.sysErrorLogService = sysErrorLogService;

	}

	// 发送系统校时
	@PostMapping("/sendSystemTiming/communication")
	//@LogConfig("发送系统校时")
	public ResultEntity sendSystemTiming(SystemSendTimingCMD systemSendTimingCMD,
										 @NonNull String host, @NonNull String updateAll) throws IOException {
		try {
			webSendToCService.sendSystemTiming(systemSendTimingCMD, host, updateAll, serialNumber);
		} catch (Exception e) {
			// receiveInformationService.receiveSocketHandler1(byteBuffer, host);
			System.out.println("发送系统校时异常");
			SysErrorLogEntity sysErrorLogEntity = new SysErrorLogEntity();
			sysErrorLogEntity.setHost(host);
			sysErrorLogEntity.setErrorMsg("发送系统校时异常");
			sysErrorLogEntity.setErrorType("系统校时异常");
			sysErrorLogEntity.setCreateTime(new Date());
			sysErrorLogService.saveError(sysErrorLogEntity);
		}
		return new ResultEntity(SystemStatusCodeEnum.SUCCESS, null);
	}

	// 添加定时发送
//	@PostMapping("/addSystemTimingTask/communication")
//	//@LogConfig("添加定时发送")
//	public ResultEntity addSystemTimingTask(String timeNow, String timingPattern, String time, String sendTime,
//											String host, String updateAll) throws SchedulerException {
//		return new ResultEntity(SystemStatusCodeEnum.SUCCESS, webSendToCService.addTimeSendTask(timeNow, time, sendTime,
//				timingPattern, host, updateAll, serialNumber));
//	}

	// 设备复位
	@PostMapping("/sendDeviceRestoration/communication")
	public ResultEntity sendDeviceRestoration(DeviceRestorationSendCMD deviceRestorationSendCMD, String host,
											  @NonNull String updateAll) {
		try {
			webSendToCService.sendDeviceRestoration(deviceRestorationSendCMD, host, updateAll, serialNumber);
		} catch (Exception e) {
			System.out.println("设备复位异常");
			SysErrorLogEntity sysErrorLogEntity = new SysErrorLogEntity();
			sysErrorLogEntity.setHost(host);
			sysErrorLogEntity.setErrorMsg("发送设备复位异常");
			sysErrorLogEntity.setErrorType("设备复位异常");
			sysErrorLogEntity.setCreateTime(new Date());
			sysErrorLogService.saveError(sysErrorLogEntity);
		}
		return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "发送指令成功");
	}

	// 设备自检指令
	@PostMapping("/sendDeviceCheckCMD/communication")
	//@LogConfig("设备自检指令")
	public ResultEntity sendDeviceCheckCMD(DeviceCheckCMD deviceCheckCMD, String host, @NonNull String updateAll) {
		try {
			webSendToCService.sendDeviceCheckCMD(deviceCheckCMD, host, updateAll, serialNumber);
		} catch (Exception e) {
			e.printStackTrace();
			SysErrorLogEntity sysErrorLogEntity = new SysErrorLogEntity();
			sysErrorLogEntity.setHost(host);
			sysErrorLogEntity.setErrorMsg("发送设备自检指令异常");
			sysErrorLogEntity.setErrorType("设备自检指令异常");
			sysErrorLogEntity.setCreateTime(new Date());
			sysErrorLogService.saveError(sysErrorLogEntity);
		}
		return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "发送指令成功");
	}

	// 软件版本更新
	@PostMapping("/sendSoftwareUpdateCMD/communication")
	//@LogConfig("软件版本更新")
	public ResultEntity sendSoftwareUpdateCMD(String timeNow, String cmd, String softwareID, String host,
											  @NonNull String updateAll) {
		try {
			webSendToCService.sendSoftwareUpdateCMD(timeNow, cmd, softwareID.trim(), host, updateAll, serialNumber);
		} catch (Exception e) {
			System.out.println("软件版本更新异常");
			SysErrorLogEntity sysErrorLogEntity = new SysErrorLogEntity();
			sysErrorLogEntity.setHost(host);
			sysErrorLogEntity.setErrorMsg("发送软件版本更新异常");
			sysErrorLogEntity.setErrorType("软件版本更新异常");
			sysErrorLogEntity.setCreateTime(new Date());
			sysErrorLogService.saveError(sysErrorLogEntity);
		}
		return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "发送指令成功");
	}

	// 设备网络参数更新指令
	@PostMapping("/sendDeviceNetworkCMD/communication")
	//@LogConfig("设备网络参数更新指令")
	public ResultEntity sendDeviceNetworkCMD(SendDeviceNetWorkParam sendDeviceNetWorkParam, String host,
											 @NonNull String updateAll) {
		try {
			webSendToCService.sendDeviceNetworkCMD(sendDeviceNetWorkParam, host, updateAll, serialNumber);
		} catch (Exception e) {
			// log.info("设备网络参数更新指令异常", e);
			System.out.println("设备网络参数更新指令异常");
			SysErrorLogEntity sysErrorLogEntity = new SysErrorLogEntity();
			sysErrorLogEntity.setHost(host);
			sysErrorLogEntity.setErrorMsg("发送设备网络参数更新指令异常");
			sysErrorLogEntity.setErrorType("设备网络参数更新指令异常");
			sysErrorLogEntity.setCreateTime(new Date());
			sysErrorLogService.saveError(sysErrorLogEntity);
		}
		return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "发送指令成功");
	}

	// 设备工作流程控制指令
	@PostMapping("/sendDeviceWorkFlowCMD/communication")
	//@LogConfig("设备工作流程控制指令")
	public ResultEntity sendDeviceWorkFlowCMD(DeviceWorkFlowCMD deviceWorkFlowCMD, SystemControlCMD systemControlCMD,
											  Integer count, String host, @NonNull String updateAll) {
		try {
			webSendToCService.sendDeviceWorkFlowCMD(deviceWorkFlowCMD, systemControlCMD, count, host, updateAll,
					serialNumber);
		} catch (Exception e) {
			// log.info("设备工作流程控制指令异常", e);
			System.out.println("设备工作流程控制指令异常");
			SysErrorLogEntity sysErrorLogEntity = new SysErrorLogEntity();
			sysErrorLogEntity.setHost(host);
			sysErrorLogEntity.setErrorMsg("发送设备工作流程控制指令");
			sysErrorLogEntity.setErrorType("设备工作流程控制指令");
			sysErrorLogEntity.setCreateTime(new Date());
			sysErrorLogService.saveError(sysErrorLogEntity);
		}
		return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "发送控制指令成功");
	}

	// 设备工作流程控制指令(雷达分机指令)(单发,群发)
	@PostMapping("/sendExtensionInstructionsCMD/communication")
	//@LogConfig("设备工作流程控制指令(雷达分机指令)")
	public ResultEntity sendExtensionInstructions(@RequestBody ArrayList<RadarDeviceCMD> deviceWorkFlowCMD,
												  @RequestHeader String deviceHost, @RequestHeader String updateAll) {
		if (512 - deviceWorkFlowCMD.size() * 32 < 0) {
			return new ResultEntity(SystemStatusCodeEnum.ERROR, "指令条数已超出");
		} else {
			try {
				webSendToCService.sendExtensionInstructionsCMD(deviceWorkFlowCMD, deviceHost, updateAll, serialNumber);
			} catch (Exception e) {
				// e.printStackTrace();
				System.out.println("设备工作流程控制指令(雷达分机指令)");
				SysErrorLogEntity sysErrorLogEntity = new SysErrorLogEntity();
				sysErrorLogEntity.setHost(deviceHost);
				sysErrorLogEntity.setErrorMsg("发送设备工作流程控制指令(雷达分机指令)");
				sysErrorLogEntity.setErrorType("设备工作流程控制指令(雷达分机指令)");
				sysErrorLogEntity.setCreateTime(new Date());
				sysErrorLogService.saveError(sysErrorLogEntity);
			}
			return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "发送雷达分机指令成功");
		}
	}

	// 设备工作流程控制指令(雷达系统指令)(单发,群发)
	@PostMapping("/sendSystemInstructionsCMD/communication")
	//@LogConfig("设备工作流程控制指令(雷达系统指令)")
	public ResultEntity sendSystemInstructions(@RequestBody ArrayList<SystemControlCMD> systemControlCMDs,
											   @RequestHeader String deviceHost, @RequestHeader String updateAll) {
		if (512 - systemControlCMDs.size() * 48 < 0) {
			return new ResultEntity(SystemStatusCodeEnum.ERROR, "指令条数已超出");
		} else {
			try {
				webSendToCService.sendSystemInstructionsCMD(systemControlCMDs, deviceHost, updateAll, serialNumber);
			} catch (Exception e) {
				System.out.println("设备工作流程控制指令(雷达系统指令)");
				SysErrorLogEntity sysErrorLogEntity = new SysErrorLogEntity();
				sysErrorLogEntity.setHost(deviceHost);
				sysErrorLogEntity.setErrorMsg("发送设备工作流程控制指令(雷达系统指令)");
				sysErrorLogEntity.setErrorType("设备工作流程控制指令(雷达系统指令)");
				sysErrorLogEntity.setCreateTime(new Date());
				sysErrorLogService.saveError(sysErrorLogEntity);
			}
			return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "发送雷达系统指令成功");
		}
	}

	// 敌我系统控制(单发,群发)
	@PostMapping("/sendEnemyAndUsSystemCMD/communication")
	//@LogConfig("敌我系统控制")
	public ResultEntity sendEnemyAndUsCMD(@RequestHeader String deviceHost, @RequestHeader String updateAll,
										  @RequestBody ArrayList<EnemyAndUsCMD> enemyAndUsCMDs) throws IOException {
		if (512 - enemyAndUsCMDs.size() * 64 < 0) {
			return new ResultEntity(SystemStatusCodeEnum.ERROR, "指令条数已超出");
		} else {
			try {
				webSendToCService.sendEnemyAndUsCMD(enemyAndUsCMDs, deviceHost, updateAll, serialNumber);
			} catch (Exception e) {
				System.out.println("敌我系统控制");
				SysErrorLogEntity sysErrorLogEntity = new SysErrorLogEntity();
				sysErrorLogEntity.setHost(deviceHost);
				sysErrorLogEntity.setErrorMsg("发送敌我系统控制");
				sysErrorLogEntity.setErrorType("敌我系统控制");
				sysErrorLogEntity.setCreateTime(new Date());
				sysErrorLogService.saveError(sysErrorLogEntity);
			}

			return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "发送敌我系统指令成功");
		}
	}

	// 查询所有
	@GetMapping
	public ResultEntity findAll() {
		return new ResultEntity(SystemStatusCodeEnum.SUCCESS, receiveInformationService.findAll());
	}
}
