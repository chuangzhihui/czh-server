package com.czh.admin.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.czh.common.annotation.Permission;
import com.czh.common.utils.JWTUtil;
import com.czh.common.utils.RedisUtil;
import com.czh.common.utils.RequestUtil;
import com.czh.service.cache.AdminAuthCache;
import com.czh.service.entity.AuthConfig;
import com.czh.service.service.AdminService;
import com.czh.service.service.AuthConfigService;
import com.czh.service.service.RoleService;
import com.czh.common.vo.EmptyVo;
import com.czh.common.vo.JSONResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@Slf4j
public class JWTAuthFilter extends OncePerRequestFilter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 上传令牌的 HMAC 签名密钥（BE-04）。
     * 必须与该令牌的签发方（{@code UploadSetServiceImpl}）使用同一份配置。
     */
    @Value("${czh.upload-token.secret}")
    private String uploadTokenSecret;

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
        String uri=request.getRequestURI();
        if(uri.equals("/upload")){
            // BE-29 治本项：Filter 内禁止抛业务异常（@ControllerAdvice 抓不到，会被 /error 抹平成 404）。
            // 校验失败时由本方法直接写响应并返回 false，随后 return，不再继续过滤器链。
            if(!checkUploadToken(request, response)){
                return;
            }
        }else{
            String token=request.getHeader("token");// 从 http 请求头中取出 token
            if(token!=null && !token.equals("false") && !token.isEmpty()){
                // 获取 token 中的 user id
                // 说明：管理员令牌此处仍是 decode（只取 adminId/ip），真正的放行判据是下面
                // 「Redis 中存的令牌字符串与传入令牌完全相等」。改为独立密钥 + 验签属于 BE-06，
                // 与本次 BE-04 不是同一条链，本轮不动（避免改一半把登录态改坏）。
                int adminId=0;
                String ip="";
                try {
                    adminId = Integer.parseInt(JWT.decode(token).getAudience().get(0));
                    ip=JWT.decode(token).getClaim("ip").asString();

                    String redisToken=redisUtil.get("adminToken_"+adminId);
                    if(RequestUtil.getIp().equals(ip)  && redisToken!=null && redisToken.equals(token)){
                        //查询登录用户（走缓存，缓存缺失时回源DB）
                        AdminAuthCache admin=adminService.getAdminCache(adminId);
                        //状态!=1 视为已冻结/已停用：不注入认证上下文，本次请求按未登录处理。
                        //（原实现不判 status，被冻结的管理员凭旧 token 仍可一路操作到 token 过期）
                        if(admin!=null && !Integer.valueOf(1).equals(admin.getStatus())){
                            log.warn("管理员状态异常,拒绝本次请求 adminId={} status={}", adminId, admin.getStatus());
                            filterChain.doFilter(request, response);
                            return;
                        }
                        if(admin!=null){
                            AuthConfig authConfig=authConfigService.getAuthConfigCache();
                            if(authConfig!=null){
                                //更新token过期时间
                                redisUtil.setEx("adminToken_"+adminId,redisToken,authConfig.getTimeOut(), TimeUnit.SECONDS);
                            }
                            AdminDetails adminDetails=new AdminDetails(admin);
                            List<SimpleGrantedAuthority> permissions=checkPermission(request,admin.getRoleId());
                            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(adminDetails, null, permissions);
                            // 将authentication信息放入到上下文对象中
                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        }
                    }
                } catch (Throwable  j) {
                    log.error("执行异常", j);
                    //这里面只做校验 不做拦截
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
        HandlerMethod handlerMethod = (HandlerMethod) requestMappingHandlerMapping.getHandler(request).getHandler();
        Method method = handlerMethod.getMethod();
        Permission permission = method.getAnnotation(Permission.class);
        if(permission!=null && !permission.required())
        {
            log.debug("免授权 {}", RequestUtil.getRequestRoute());
            return adminPermissions;
        }
        //需要校验权限
        log.debug("需要校验权限 {}", RequestUtil.getRequestRoute());
        List<String> permissions =roleService.getRolePermissions(roleId);
        //当前路由关联权限
        List<String> routePermissions= permission!=null?new ArrayList<>(Arrays.asList(permission.permissionList())):new ArrayList<>();
        routePermissions.add(RequestUtil.getRequestRoute());
        List<String> intersection = routePermissions.stream()
                .filter(permissions::contains)
                .collect(Collectors.toList());
        if(intersection.isEmpty())
        {
            log.warn("无权限 roleId={} route={}", roleId, RequestUtil.getRequestRoute());
            //如果交集为空 则无权限
            return new ArrayList<>();
        }
        return adminPermissions;
    }

    /**
     * 校验上传token（BE-04：真正验签 + BE-29：Filter 内不抛业务异常）
     * <p>
     * 原实现全程 {@code JWT.decode(token)} 只解码不验签，而签名密钥是源码里的字面量常量
     * {@code "userGetUploadToken"}，攻击者本地就能签一张自己的令牌。现改为
     * {@link JWTUtil#verify(String, String)} 验签，密钥来自配置项 {@code czh.upload-token.secret}。
     *
     * @param request  当前请求
     * @param response 校验失败时用于写出响应
     * @return 校验通过返回 true；失败返回 false（响应已写出，调用方必须 return）
     */
    private boolean checkUploadToken(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String token = request.getHeader("token");
        if(token==null || token.equals("false") || token.isEmpty())
        {
            log.warn("上传请求缺少 token uri={} ip={}", request.getRequestURI(), RequestUtil.getIp());
            writeJson(response, 401, "上传凭证缺失,请重新获取");
            return false;
        }
        try {
            // 真验签：签名不对 / 已过期都会抛 JWTVerificationException
            DecodedJWT jwt = JWTUtil.verify(token, uploadTokenSecret);
            List<String> audienceList = jwt.getAudience();
            String audience = (audienceList == null || audienceList.isEmpty()) ? "" : audienceList.get(0);
            String ip = jwt.getClaim("ip").asString();

            if(!JWTUtil.UPLOAD_TOKEN_AUDIENCE.equals(audience) || !RequestUtil.getIp().equals(ip)){
                log.warn("上传 token 校验失败 audience={} ip={}", audience, RequestUtil.getIp());
                writeJson(response, 401, "上传凭证无效,请重新获取");
                return false;
            }
            return true;
        } catch (JWTVerificationException e) {
            // 验签失败/过期：这是安全事件，必须 warn 出来（BE-29：不能再被 /error 抹成 404）
            log.warn("上传 token 验签失败 ip={} msg={}", RequestUtil.getIp(), e.getMessage());
            writeJson(response, 401, "上传凭证无效或已过期,请重新获取");
            return false;
        } catch (Throwable  j) {
            log.error("上传 token 校验异常", j);
            writeJson(response, 401, "上传凭证校验异常,请重新获取");
            return false;
        }
    }

    /**
     * 写统一 JSON 响应。
     * <p>
     * 全项目约定：<b>HTTP 状态码层恒 200</b>，业务结果看响应体 code（见 BE-29）。
     * 这里是 Filter 层，不走 {@code @ControllerAdvice}，因此手工写一次。
     *
     * @param response 响应对象
     * @param code     业务码
     * @param msg      提示文案
     */
    private void writeJson(HttpServletResponse response, int code, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=utf-8");
        JSONResult<EmptyVo> res = new JSONResult<>();
        res.code = code;
        res.msg = msg;
        OBJECT_MAPPER.writeValue(response.getOutputStream(), res);
    }
}
