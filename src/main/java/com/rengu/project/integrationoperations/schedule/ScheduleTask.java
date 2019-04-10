//package com.rengu.project.integrationoperations.schedule;
//
//import com.rengu.project.integrationoperations.entity.UserEntity;
//import com.rengu.project.integrationoperations.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//
//import java.util.List;
//
///**
// * @Author: yaojiahao
// * @Date: 2019/4/10 9:26
// */
//public class ScheduleTask {
//    @Autowired
//    private final UserRepository userRepository;
//
//    public ScheduleTask(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    /**
//     * 超时事件检查
//     */
//    @Scheduled(cron = "0 0/1 * * * ?")
//    public void ScanUserOnline() {
//        //获取所有在线用户
//        List<UserEntity> users = userRepository.getUsersWithOnLine(4);
//        users.stream().parallel().forEach(user -> {
//            //通过时间判断是否session过期
//            if (CommonUtil.CalculationData(user.getAccessLastTime(),30)) {
//                //如果过期则设置用户为下下线状态
//                updateOnline(user.getId(),4,null);
//                //session 置为失效  SessionUtil.expireSession(null,user,sessionRegistry);
//            }
//        });
//    }
//
//
//    /**
//     * session 失效
//     * @param request
//     * @param sessionRegistry
//     */
//    public static void expireSession(HttpServletRequest request,User user, SessionRegistry sessionRegistry) {
//        List<SessionInformation> sessionsInfo = null;
//        if (null != user) {
//            List<Object> o = sessionRegistry.getAllPrincipals();
//            for (Object principal : o) {
//                if (principal instanceof User && (user.getUsername().equals(((User) principal).getUsername()))) {
//                    sessionsInfo = sessionRegistry.getAllSessions(principal, false);
//                }
//            }
//        } else if (null != request) {
//            SecurityContext sc = (SecurityContext) request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
//            if (null != sc.getAuthentication().getPrincipal()) {
//                sessionsInfo = sessionRegistry.getAllSessions(sc.getAuthentication().getPrincipal(), false);
//                sc.setAuthentication(null);
//            }
//        }
//        if (null != sessionsInfo && sessionsInfo.size() > 0) {
//            for (SessionInformation sessionInformation : sessionsInfo) {
//                //当前session失效
//                sessionInformation.expireNow();
//                sessionRegistry.removeSessionInformation(sessionInformation.getSessionId());
//            }
//        }
//    }
//}
//
