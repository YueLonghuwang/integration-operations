package com.rengu.project.integrationoperations.configuration;

import com.rengu.project.integrationoperations.entity.SysLogEntity;
import com.rengu.project.integrationoperations.service.SysLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.annotations.common.util.impl.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * @author yyc
 * @date 2019/9/12 9:00
 * 日志插入到数据库
 */
@Aspect
@Component
@Slf4j
public class LogAsPectConfig {
    @Autowired
    private SysLogService sysLogService;
    //表示匹配带有自定义注解的方法
    @Pointcut("@annotation(com.rengu.project.integrationoperations.configuration.LogConfig)")
    public void pointcut(){}
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) {
        Object result =null;
        long beginTime = System.currentTimeMillis();
        try {
            log.info("我在目标方法之前执行！");
            result = point.proceed();
            long endTime = System.currentTimeMillis();
            insertLog(point,endTime-beginTime);
        } catch (Throwable e) {
            // TODO Auto-generated catch block
        }
        return result;
    }
    private void insertLog(ProceedingJoinPoint point ,long time) {
        MethodSignature signature = (MethodSignature)point.getSignature();
        Method method = signature.getMethod();
        SysLogEntity sys_log = new SysLogEntity();
        //Log userAction = (Log) method.getAnnotation(LogConfig.class);
        LogConfig userAction=method.getAnnotation(LogConfig.class);
        if (userAction != null) {
            // 注解上的描述
            sys_log.setUserAction(userAction.value());
        }
        // 请求的类名
        String className = point.getTarget().getClass().getName();
        // 请求的方法名
        String methodName = signature.getName();
        // 请求的方法参数值
        String args = Arrays.toString(point.getArgs());
        //从session中获取当前登陆人id
//		Long useride = (Long)SecurityUtils.getSubject().getSession().getAttribute("userid");
        //Long userid = 1L;//应该从session中获取当前登录人的id，这里简单模拟下
        //sys_log.setUserId(userid);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String times=simpleDateFormat.format(sys_log.getCreateTime());

        // sys_log.setCreateTime(new java.sql.Timestamp(new Date().getTime()));
        //log.info("当前登陆人：{},类名:{},方法名:{},参数：{},执行时间：{}",userid, className, methodName, args, time);
        sysLogService.saveLog(sys_log);
    }
}
