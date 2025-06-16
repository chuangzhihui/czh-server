package com.czh.admin.config;

import com.czh.admin.security.AdminAuthFailedHandler;
import com.czh.admin.security.AdminDetailsService;
import com.czh.admin.security.JWTAuthFilter;
import com.czh.common.config.CZHPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    AdminDetailsService adminDetailsService;
    @Autowired
    AdminAuthFailedHandler adminAuthFailedHandler;
    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider=new DaoAuthenticationProvider();
        //将编写的UserDetailsService注入进来
        provider.setUserDetailsService(adminDetailsService);
        //将使用的密码编译器加入进来
        provider.setPasswordEncoder(passwordEncoder);
        //将provider放置到AuthenticationManager 中
        return new ProviderManager(provider);
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


    @Autowired
    CorsFilter corsFilter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        //关闭csrf
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.authorizeHttpRequests(it->
                it.requestMatchers("/admin/login/*").permitAll()  //设置登录路径所有人都可以访问
                        .requestMatchers("/uploads/*/*").permitAll()//静态最好配置为Nginx访问
                        .requestMatchers("/upload").permitAll()//上传接口单独token校验
                        .requestMatchers("/error").permitAll()
                        .anyRequest().hasAuthority("admin")  //其他路径都要进行拦截

        ).exceptionHandling(exception->exception.authenticationEntryPoint(adminAuthFailedHandler).accessDeniedHandler(adminAuthFailedHandler));
        // 开启登录认证流程过滤器
        httpSecurity.addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        httpSecurity.addFilterBefore(corsFilter, JWTAuthFilter.class);

        return httpSecurity.build();
    }
}
