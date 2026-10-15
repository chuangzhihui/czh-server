package com.czh.admin.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 跨域过滤器
 * <p>
 * 只允许白名单内的来源跨域访问：<b>不再原样反射请求方的 Origin</b>，
 * 避免任意站点在携带凭证的情况下读取本服务响应。
 * <p>
 * 白名单通过 {@code czh.cors.allowed-origins} 配置（逗号分隔），
 * 未命中时不输出任何 CORS 响应头，由浏览器同源策略拦截。
 */
@Slf4j
@Component
public class CorsFilter implements Filter {

    /**
     * 允许的跨域来源白名单，逗号分隔。
     * 默认覆盖：本地开发（Vite 5173）与生产后台域名。
     */
    @Value("${czh.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,https://java.cqczh.com.cn:10443}")
    private String allowedOrigins;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        /* 自己的代码 */
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String origin = req.getHeader("Origin");
        String allowOrigin = resolveAllowedOrigin(origin);
        if (allowOrigin != null) {
            //允许请求携带认证信息(cookie)；* 通配符不能与凭证同用，故此时不输出该头
            if (!"*".equals(allowOrigin)) {
                res.setHeader("Access-Control-Allow-Credentials", "true");
            }
            //只回显白名单内的来源，绝不原样反射请求方 Origin
            res.setHeader("Access-Control-Allow-Origin", allowOrigin);
            //响应随 Origin 变化，避免中间缓存把 A 站的许可返回给 B 站
            res.setHeader("Vary", "Origin");
            //允许请求的类型
            res.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
            //允许的请求头字段
            res.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept,Token,access-control-allow-origin");
            //设置预检请求的有效期
            //浏览器同源策略：出于安全考虑，浏览器限制跨域的http请求。怎样限制呢？通过发送两次请求：预检请求、用户请求。
            //1、预检请求作用：获知服务器是否允许该跨域请求：如果允许，才发起第二次真实的请求；如果不允许，则拦截第二次请求
            //2、单位:s,在此期间不用发送预检请求。
            //3、若为0：表示每次请求都发送预检请求,每个ajax请求之前都会先发送预检请求。
            res.setHeader("Access-Control-Max-Age", "3600");
        } else if (origin != null && !origin.isEmpty()) {
            //未命中白名单：不输出任何 CORS 头，由浏览器同源策略拦截；记录来源便于排查
            log.warn("CORS 拒绝来源: {} , URI: {}", origin, req.getRequestURI());
        }
        //OPTIONS Method表示浏览器发送的预检请求。
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(allowOrigin != null ? HttpServletResponse.SC_OK : HttpServletResponse.SC_FORBIDDEN);
        } else {
            log.info(req.getRequestURI());
            /* 自己的代码 */
            chain.doFilter(req, res);
        }
    }

    /**
     * 判断请求来源是否在白名单内
     *
     * @param origin 请求头 Origin；同源请求或服务端直连调用时为 null
     * @return 允许时返回该 Origin（或 * 通配）；不允许时返回 null
     */
    private String resolveAllowedOrigin(String origin) {
        if (allowedOrigins == null || allowedOrigins.trim().isEmpty()) {
            return null;
        }
        List<String> whiteList = Arrays.asList(allowedOrigins.split(","));
        if (whiteList.contains("*")) {
            return "*";
        }
        //Origin 为空说明是同源请求或服务端调用，无需 CORS 头
        if (origin == null || origin.trim().isEmpty()) {
            return null;
        }
        for (String allowed : whiteList) {
            if (origin.equalsIgnoreCase(allowed.trim())) {
                return allowed.trim();
            }
        }
        return null;
    }

    @Override
    public void destroy() {
        /* 自己的代码 */

    }
}
