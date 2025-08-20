package com.example.videoapp.config;

import com.example.videoapp.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类，配置认证和授权规则
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     * JWT认证过滤器
     */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置密码编码器Bean
     * 
     * @return BCrypt密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置安全过滤器链
     * 
     * @param http HTTP安全配置对象
     * @return 配置好的安全过滤器链
     * @throws Exception 配置过程中的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF保护
                .csrf(csrf -> csrf.disable())
                // 使用无状态会话管理策略
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 公开的POST接口(注册、登录、发送验证码)
                        .requestMatchers(HttpMethod.POST,
                                "/api/users/register",
                                "/api/users/login",
                                "/api/users/send-verification-code").permitAll()

                        // 公开的GET接口(视频、评论、用户公开信息)
                        .requestMatchers(HttpMethod.GET,
                                "/api/videos/**",
                                "/api/videos/{videoId}/comments",
                                "/api/users/search",
                                "/api/users/{username}",
                                "/api/users/{userId}/videos"
                        ).permitAll()

                        // 公开的静态资源
                        .requestMatchers("/videos/**", "/covers/**", "/avatars/**").permitAll()

                        // 明确指定需要认证的API
                        .requestMatchers("/api/users/me", "/api/notifications/**").authenticated()

                        // 其他任何请求都需要认证
                        .anyRequest().authenticated()
                );

        // 在用户名密码认证过滤器之前添加JWT认证过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}