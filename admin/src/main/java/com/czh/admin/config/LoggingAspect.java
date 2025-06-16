package com.czh.admin.config;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.czh.admin.security.AdminDetails;
import com.czh.common.annotation.Log;
import com.czh.common.utils.DateUtil;
import com.czh.common.utils.RequestUtil;
import com.czh.common.vo.JSONResult;
import com.czh.service.entity.Admin;
import com.czh.service.entity.AdminActionLog;
import com.czh.service.service.AdminService;
import com.czh.service.service.AsyncService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Log4j2
public class LoggingAspect {
    @Autowired
    HttpServletRequest request;
    @Autowired
    private AsyncService asyncService;
    @Autowired
    @Lazy
    AdminService adminService;
    @Pointcut("@annotation(com.czh.common.annotation.Log)")
    public void pointCut(){}

    @Around("@annotation(com.czh.common.annotation.Log)")
    public JSONResult afterReturning(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("进入切面");
        AdminActionLog adminActionLog = new AdminActionLog();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log logAnno = method.getAnnotation(Log.class);
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = method.getName();
        methodName = className +"-"+ methodName;
        Object[] args = joinPoint.getArgs();
//        ObjectMapper objectMapper = new ObjectMapper();
        String params ="";
        if(args.length>0)
        {
            params=JSON.toJSONString(args[0]);
        }
        String operation = logAnno.operation();
        String username = "";
        int userId=0;
        log.info("methodName:{}",methodName);
        if (methodName.contains("login")) {

            JSONObject object=JSONObject.parseObject(params);
            if(object!=null)
            {
                String name=object.getString("username");
                if(name!=null)
                {
                    username = JSONObject.parseObject(params).get("username").toString();
                    Admin admin= adminService.getAdminByUsername(username);
                    if(admin!=null)
                    {
                        userId=admin.getAdminId();
                    }
                }
            }
        }else{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authentication!=null && authentication.isAuthenticated() && authentication.getPrincipal()!=null)
            {
                AdminDetails adminDetails= (AdminDetails) authentication.getPrincipal();
                if(adminDetails!=null)
                {
                    username=adminDetails.getUsername();
                    userId=adminDetails.getAdminId();
                }
            }
        }


        adminActionLog.setOperation(operation);
        adminActionLog.setMethod(methodName);
        adminActionLog.setParams(params);
        adminActionLog.setIp(RequestUtil.getIp());
        adminActionLog.setUserName(username);
        adminActionLog.setUserID(userId);
        adminActionLog.setCreateTime(DateUtil.getStrDate("yyyy-MM-dd HH:mm:ss"));
        String resultJsonString=JSON.toJSONString(joinPoint.proceed());
        adminActionLog.setBake(resultJsonString);
        asyncService.addAdminLog(adminActionLog);
        JSONResult result=JSON.parseObject(resultJsonString,JSONResult.class);
        return result;
    }

}
