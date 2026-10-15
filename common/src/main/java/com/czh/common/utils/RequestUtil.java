package com.czh.common.utils;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Objects;

/**
 *  Request工具类
 *  +----------------------------------------------------------------------
 *  | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2025 https://www.crmeb.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 *  +----------------------------------------------------------------------
 *  | Author: CRMEB Team <admin@crmeb.com>
 *  +----------------------------------------------------------------------
 */
@Slf4j
public class RequestUtil extends HttpServlet {


    public static HttpServletRequest getRequest() {
        if(RequestContextHolder.getRequestAttributes() != null){
            return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        }
        return null;
    }
    public static String getIp(){
        HttpServletRequest request=getRequest();
        // 优先取 X-Real-IP
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("x-forwarded-for");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getRemoteAddr();
            if ("0:0:0:0:0:0:0:1".equals(ip))
            {
                ip = "unknown";
            }
        }
        if ("unknown".equalsIgnoreCase(ip)){
            ip = "unknown";
            return ip;
        }
        int index = ip.indexOf(',');
        if (index >= 0){
            ip = ip.substring(0, index);
        }
        return ip;
    }
    public static HashMap<String, Object> getRequestParamAndHeader(){

        try{
            HttpServletRequest request = getRequest();
            if(request == null){
                return null;
            }

            //request信息
            HashMap<String, Object> data = new HashMap<>();
            HashMap<String, Object> requestParams = new HashMap<>();
            Enumeration<String> paraNames = request.getParameterNames();
            if(paraNames != null){
                for(Enumeration<String> enumeration =paraNames;enumeration.hasMoreElements();){
                    String key= enumeration.nextElement();
                    requestParams.put(key, request.getParameter(key));
                }
            }

            HashMap<String, Object> requestFilter = new HashMap<>();
            Enumeration<String> attributeNames = request.getAttributeNames();
            if(attributeNames != null){
                for ( Enumeration<String> attributeNames1 = attributeNames; attributeNames1.hasMoreElements();) {
                    String key= attributeNames1.nextElement();
                    if(key.contains("request_")){
                        requestFilter.put(key, request.getAttribute(key));
                    }
                }
            }

            data.put("url", request.getRequestURL());
            data.put("uri", request.getRequestURI());
            data.put("method", request.getMethod());
            data.put("request", requestParams);
            data.put("request_filter", requestFilter);

            //header 信息
            Enumeration<String> headerNames = request.getHeaderNames();
            HashMap<String, Object> headerParams = new HashMap<>();
            if(headerNames != null){
                for(Enumeration<String> enumeration = headerNames;enumeration.hasMoreElements();){
                    String key= enumeration.nextElement();
                    String value=request.getHeader(key);
                    headerParams.put(key, value);
                }
            }
            data.put("header", headerParams);


            return data;
        }catch (Exception e){
            log.error("执行异常", e);
            return null;
        }
    }

    public static String getDomain(){
        HttpServletRequest request = getRequest();
        if(request == null){
            return null;
        }
        return request.getServerName() + ":" + request.getServerPort();
    }


    /**
     * 拼出完整的请求路由字符串，用于与数据库中的权限路由（{@code menu.route}）比对。
     * <p>
     * 两条历史坑，都不要改回去：
     * <ol>
     *   <li><b>不能固定循环 4 次</b>：URI 段数可能不足 4（如 {@code /admin/role}），
     *       固定循环会抛 {@link ArrayIndexOutOfBoundsException}，被上层
     *       {@code catch (Throwable)} 吞掉后等价于"无权限"——鉴权不应依赖异常兜底。
     *       因此必须按实际段数取，不足时返回已有部分。</li>
     *   <li><b>不能再加 {@code Math.min(uris.length, 4)} 之类的截断</b>：
     *       旧实现只取前 4 段，5 段及以上的路由（如 {@code /admin/user/import/template}）
     *       会被静默截成父路由 {@code /admin/user/import}，从而继承父路由的权限——
     *       这是<b>权限静默放大</b>。当前 48 个端点恰好都 ≤4 段所以没暴露，
     *       但作为会被反复 fork 的模板，一旦下游项目出现 5 段路由就是安全缺陷（BE-31②）。</li>
     * </ol>
     *
     * @return 完整路由字符串；无请求上下文时返回空串
     */
    public static String getRequestRoute() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "";
        }
        String[] uris = request.getRequestURI().split("/");
        // 必须按实际段数全量拼接：截断会让 5 段以上路由继承父路由权限（权限静默放大）
        int len = uris.length;
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < len; i++) {
            route.append(uris[i]);
            if (i < len - 1) {
                route.append("/");
            }
        }
        return route.toString();
    }
}
