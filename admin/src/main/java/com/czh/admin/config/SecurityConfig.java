package com.czh.admin.config;

import com.czh.admin.security.AdminAuthenticationProvider;
import com.czh.admin.security.AdminAuthFailedHandler;
import com.czh.admin.security.CaptchaService;
import com.czh.admin.security.JWTAuthFilter;
import com.czh.common.config.CZHPasswordEncoder;
import com.czh.service.service.AdminService;
import com.czh.service.service.AuthConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    AdminAuthFailedHandler adminAuthFailedHandler;
    @Autowired
    CorsFilter corsFilter;
    /**
     * 认证 Provider：DaoAuthenticationProvider 的子类，额外负责验证码校验与失败计数/冻结
     */
    @Bean
    public AdminAuthenticationProvider adminAuthenticationProvider(PasswordEncoder passwordEncoder,
                                                                   AdminService adminService,
                                                                   AuthConfigService authConfigService,
                                                                   CaptchaService captchaService) {
        AdminAuthenticationProvider provider =
                new AdminAuthenticationProvider(adminService, authConfigService, captchaService);
        //UserDetails 来源由 Provider 自己实现（loadUserByUsername），只需注入密码编码器
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AdminAuthenticationProvider adminAuthenticationProvider) {
        //将provider放置到AuthenticationManager 中
        return new ProviderManager(adminAuthenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new CZHPasswordEncoder();
    }
    /**
     * token认证过滤器
     */
    @Bean
    public JWTAuthFilter jwtAuthFilter() {
        return new JWTAuthFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        //关闭csrf
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        //本项目为 JWT 无状态鉴权：不创建、不使用 HttpSession，
        //避免 SecurityContext（含管理员信息）被写入 Session
        httpSecurity.sessionManagement(session->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        httpSecurity.authorizeHttpRequests(it->
                it.requestMatchers("/admin/login/*").permitAll()  //设置登录路径所有人都可以访问
                        .requestMatchers("/uploads/*/*").permitAll()//静态最好配置为Nginx访问
                        // 上传接口：保留 permitAll（业务确需匿名上传 —— 后台文件库/富文本组件
                        // 拿到 getUploadToken 下发的短期令牌后直接打 /upload，不走登录态）。
                        // 但"匿名"不等于"不校验"，它的唯一关卡是 JWTAuthFilter#checkUploadToken：
                        //   · 真验签（密钥来自 czh.upload-token.secret，不再是源码常量）—— BE-04；
                        //   · 令牌 600 秒短期有效 + 绑定签发时的 IP；
                        //   · 校验失败直接写 401 响应，不再被 /error 抹成 404 —— BE-29。
                        // 已修（BE-07）：扩展名白名单统一收口在 UploadController#doUpload（五家分支共用），
                        // 并按 type 分组（图片/视频/Excel/word/pdf/压缩包）；大小限制交由
                        // spring.servlet.multipart 配置，如需按类型差异化再补。
                        .requestMatchers("/upload").permitAll()//上传接口单独token校验
                        .requestMatchers("/error").permitAll()
                        .anyRequest().hasAuthority("admin")  //其他路径都要进行拦截

        ).exceptionHandling(exception->exception.authenticationEntryPoint(adminAuthFailedHandler).accessDeniedHandler(adminAuthFailedHandler));
        // 开启登录认证流程过滤器
        // 注意：CorsFilter 必须在 security chain 内、且位于所有认证/授权之前——
        // 401/403 响应可能在本 chain 内直接写出并提交，若 CorsFilter 排在它后面
        // （比如只靠 Boot 的容器级注册，其默认顺序最低），CORS 头就永远写不上，
        // 前端会看到跨域报错。
        httpSecurity.addFilterBefore(corsFilter, LogoutFilter.class);
        httpSecurity.addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    /**
     * 禁用 Boot 对 CorsFilter 的容器级自动注册：它已作为第一个过滤器加入 SecurityFilterChain，
     * 再注册一次会导致每个请求执行两次（重复过白名单、重复写响应头）。
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsFilter corsFilter) {
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(corsFilter);
        registration.setEnabled(false);
        return registration;
    }
}
