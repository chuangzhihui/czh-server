package com.czh.admin.security;

import com.auth0.jwt.JWT;
import com.czh.common.annotation.Permission;
import com.czh.common.exception.ErrorException;
import com.czh.common.utils.RedisUtil;
import com.czh.common.utils.RequestUtil;
import com.czh.service.entity.Admin;
import com.czh.service.entity.AuthConfig;
import com.czh.service.service.AdminService;
import com.czh.service.service.AuthConfigService;
import com.czh.service.service.RoleService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
public class JWTAuthFilter extends OncePerRequestFilter {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    AdminService adminService;
    @Autowired
    AuthConfigService authConfigService;
    @Autowired
    RoleService roleService;
    @Autowired
    RequestMappingHandlerMapping requestMappingHandlerMapping;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.error("JWTAuthFilter{}",request.getRequestURI());
        String uri=request.getRequestURI();
        if(uri.equals("/upload")){
            String uptoken=request.getHeader("token");// 从 http 请求头中取出 token
            if(uptoken==null || !redisUtil.hasKey(uptoken)){
                throw new ErrorException("token校验失败!");
            }
        }else{
            String token=request.getHeader("token");// 从 http 请求头中取出 token
            if(token!=null && !token.equals("false") && !token.isEmpty()){
                // 获取 token 中的 user id
                int adminId=0;
                String ip="";
                try {
                    adminId = Integer.parseInt(JWT.decode(token).getAudience().get(0));
                    ip=JWT.decode(token).getClaim("ip").asString();
                    String redisToken=redisUtil.get("adminToken_"+adminId);
                    if(RequestUtil.getIp().equals(ip) && redisToken!=null && redisToken.equals(token)){
                        //查询登录用户
                        Admin admin=adminService.getById(adminId);
                        if(admin!=null){
                            AuthConfig authConfig=authConfigService.getById(1);
                            //更新token过期时间
                            redisUtil.setEx("adminToken_"+adminId,redisToken,authConfig.getTimeOut(), TimeUnit.SECONDS);


                            checkPermission(request,admin.getRoleId());
                            AdminDetails adminDetails=new AdminDetails(admin);
                            List<SimpleGrantedAuthority> permissions=checkPermission(request,admin.getRoleId());
//                            adminDetails.setPermissions(permissions);
//                            adminDetails.geta
//                            adminDetails.set
                            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(adminDetails, null, permissions);
                            // 将authentication信息放入到上下文对象中
                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        }
                    }
                } catch (Throwable  j) {
                    //这里面只做校验 不做拦截
//                    throw new NotLoginException();
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 权限校验
     * @param request
     */
    private List<SimpleGrantedAuthority> checkPermission(HttpServletRequest request, Integer roleId) throws Exception {


        List<SimpleGrantedAuthority> adminPermissions = new ArrayList<>();
        adminPermissions.add(new SimpleGrantedAuthority("admin"));
        HandlerMethod handlerMethod = (HandlerMethod) requestMappingHandlerMapping
                .getHandler(request).getHandler();
        Method method = handlerMethod.getMethod();
        Permission permission = method.getAnnotation(Permission.class);
        if(permission!=null && !permission.required())
        {
            log.info("免授权");
            return adminPermissions;
        }
        //需要校验权限
        log.info("需要校验权限");
        List<String> permissions =roleService.getRolePermissions(roleId);
        //当前路由关联权限
        List<String> routePermissions= permission!=null?new ArrayList<>(Arrays.asList(permission.permissionList())):new ArrayList<>();
        routePermissions.add(RequestUtil.getRequestRoute());
        List<String> intersection = routePermissions.stream()
                .filter(permissions::contains)
                .collect(Collectors.toList());
        if(intersection.isEmpty())
        {
            log.error("无权限");
            //如果交集为空 则无权限
//            throw new NoAuthException();
            return new ArrayList<>();
        }
        return adminPermissions;
    }
}
