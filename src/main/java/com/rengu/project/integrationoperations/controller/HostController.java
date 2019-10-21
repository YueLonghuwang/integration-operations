package com.rengu.project.integrationoperations.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rengu.project.integrationoperations.entity.AllHost;
import com.rengu.project.integrationoperations.entity.ResultEntity;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.service.HostService;

@RestController
@RequestMapping(value="/modify")
public class HostController {

	private final HostService hostService;

	@Autowired
	public HostController(HostService hostService) {
		this.hostService = hostService;
	}

	//根据id修改站id或ip
	@PatchMapping(value="/{hostId}")
	public ResultEntity updataHostById(@PathVariable(value="hostId") String hostId,AllHost allHost) {
		hostService.updateIDAndIP(hostId, allHost);
		return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "修改成功");
	}
}
