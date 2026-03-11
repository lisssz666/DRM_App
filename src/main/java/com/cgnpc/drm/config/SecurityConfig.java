package com.cgnpc.drm.config;

import com.cgnpc.drm.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全配置类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码加密器
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置 HttpSecurity
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // 禁用 CSRF 保护
                .authorizeRequests()
                .antMatchers("/api/user/register", "/api/user/login", "/api/user/sendCode", "/api/user/verifyCode", "/api/user/forgotPassword", "/api/user/checkEmail", "/api/user/checkPhone", "/api/user/logout").permitAll() // 允许访问用户相关接口
                .antMatchers("/api/device/**", "/api/workingMode/**").permitAll() // 允许访问设备管理和工作模式接口
                .anyRequest().authenticated() // 其他接口需要认证
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 无状态会话
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // 添加 JWT 过滤器
                .formLogin().disable() // 禁用默认的表单登录
                .httpBasic().disable(); // 禁用 HTTP 基本认证
    }
}