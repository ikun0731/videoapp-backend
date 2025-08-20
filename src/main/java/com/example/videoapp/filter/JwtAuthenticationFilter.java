package com.example.videoapp.filter;

import com.example.videoapp.JwtUtil;
import com.example.videoapp.service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器，处理HTTP请求中的JWT令牌验证
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * JWT工具类
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户详情服务
     */
    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * 执行过滤器内部逻辑
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {

        // 从请求头获取Authorization信息
        final String authHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 检查请求头是否包含有效的Bearer令牌
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 提取JWT令牌（去掉"Bearer "前缀）
            jwt = authHeader.substring(7);
            try {
                // 从令牌中获取用户名
                username = jwtUtil.getUsernameFromToken(jwt);
            } catch (IllegalArgumentException e) {
                logger.warn("无法获取JWT令牌");
            } catch (ExpiredJwtException e) {
                logger.warn("JWT令牌已过期");
            } catch (SecurityException e) {
                logger.warn("JWT签名无效");
            }
        }

        // 当成功获取用户名，且SecurityContext中当前没有认证信息时
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 从用户服务加载用户详情
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 创建认证令牌
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            // 将认证信息设置到SecurityContext中
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}
