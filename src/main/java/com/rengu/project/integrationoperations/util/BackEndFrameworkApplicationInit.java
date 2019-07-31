package com.rengu.project.integrationoperations.util;

import com.rengu.project.integrationoperations.entity.AllHost;
import com.rengu.project.integrationoperations.entity.CMDSerialNumber;
import com.rengu.project.integrationoperations.entity.RoleEntity;
import com.rengu.project.integrationoperations.entity.UserEntity;
import com.rengu.project.integrationoperations.enums.SystemRoleEnum;
import com.rengu.project.integrationoperations.enums.SystemUserEnum;
import com.rengu.project.integrationoperations.repository.CMDSerialNumberRepository;
import com.rengu.project.integrationoperations.repository.HostRepository;
import com.rengu.project.integrationoperations.service.WebSendToCService;
import com.rengu.project.integrationoperations.service.RoleService;
import com.rengu.project.integrationoperations.service.UserService;
import com.rengu.project.integrationoperations.thread.TCPThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author hanchangming
 * @date 2019-03-25
 */

@Slf4j
@Order(value = -1)
@Component
public class BackEndFrameworkApplicationInit implements ApplicationRunner {
    private final TCPThread tcpThread;
    private final RoleService roleService;
    private final UserService userService;
    private final HostRepository hostRepository;
    private final WebSendToCService webSendToCService;
    private final CMDSerialNumberRepository cmdSerialNumberRepository;
    private final JavaClientUtil javaClientUtil;
    @Autowired
    public BackEndFrameworkApplicationInit(TCPThread tcpThread, RoleService roleService, UserService userService, HostRepository hostRepository, WebSendToCService webSendToCService, CMDSerialNumberRepository cmdSerialNumberRepository, JavaClientUtil javaClientUtil) {
        this.tcpThread = tcpThread;
        this.roleService = roleService;
        this.userService = userService;
        this.hostRepository = hostRepository;
        this.webSendToCService = webSendToCService;
        this.cmdSerialNumberRepository = cmdSerialNumberRepository;
//        this.javaClientUtil = javaClientUtil;
        this.javaClientUtil = javaClientUtil;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 建立系统角色

        List<RoleEntity> roleEntityList = new ArrayList<>();
        for (SystemRoleEnum systemRoleEnum : SystemRoleEnum.values()) {
            if (!roleService.hasRoleByName(systemRoleEnum.getName())) {
                RoleEntity roleEntity = new RoleEntity(systemRoleEnum);
                roleEntityList.add(roleEntity);
            }
        }
        if (!roleEntityList.isEmpty()) {
            roleService.saveRoles(roleEntityList);
            log.info("系统成功初始化" + roleEntityList.size() + "个角色");
        }
        //  建立系统用户
        List<UserEntity> userEntityList = new ArrayList<>();
        for (SystemUserEnum systemUserEnum : SystemUserEnum.values()) {
            if (!userService.hasUserByUsername(systemUserEnum.getUsername())) {
                UserEntity userEntity = new UserEntity(systemUserEnum);
                Set<RoleEntity> roleEntitySet = new HashSet<>();
                for (SystemRoleEnum systemRoleEnum : systemUserEnum.getSystemRoleEnums()) {
                    roleEntitySet.add(roleService.getRoleByName(systemRoleEnum.getName()));
                }
                userEntity.setRoles(roleEntitySet);
                userEntityList.add(userEntity);
            }
        }
        //  初始化编号
        if (cmdSerialNumberRepository.findAll().size() == 0) {
            CMDSerialNumber cmdSerialNumber = new CMDSerialNumber();
            cmdSerialNumber.setSerialNumber(0);
            cmdSerialNumberRepository.save(cmdSerialNumber);
        }
        if (!userEntityList.isEmpty()) {
            userService.saveUsers(userEntityList);
            log.info("系统成功初始化" + userEntityList.size() + "个用户");
        }
        // 初始化host
        int hostSize = hostRepository.findAll().size();

        // 3 代表的 3台设备 如果数据库没有三台设备信息，那么补充起来 以方便前端显示 不然前端处理较为复杂
        if(hostSize<3){
            // 声明一个数 进行数量的增加
            int b = 1;
            List<AllHost> allHostList1=hostRepository.findAll();
            List<AllHost> allHostList = new ArrayList<>();
            for(int a=0; a<3-hostSize;a++){
                // 如果存在一个或者两个IP那么重置当前IP的num
                if(hostSize>0){
                   for(AllHost allHost : allHostList1){
                       allHost.setNum(b++);
                       hostRepository.save(allHost);
                   }
                }
                AllHost allHost = new AllHost();
                allHost.setHost("无");
                allHost.setNum(b++);
                allHostList.add(allHost);
            }
            hostRepository.saveAll(allHostList);
        }
        tcpThread.monitoringTCP();
    }
}