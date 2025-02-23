package com.twitter.twitter_rest_api.security;

import com.twitter.twitter_rest_api.entity.User;
import com.twitter.twitter_rest_api.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = null;
            Cookie[] cookies=request.getCookies();



            if(cookies!=null){
                for (Cookie cookie:cookies){
                    if("accessToken".equals(cookie.getName())){
                        jwt=cookie.getValue();
                        break;
                    }
                }
            }
            log.debug("JWT token from cookie: {}", jwt);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                log.debug("Username from JWT token: {}", username);

                UserDetails user = (UserDetails) userService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Kullanıcı kimlik doğrulaması başarılı: {}", username);
            } else {
                log.debug("JWT token geçersiz veya bulunamadı");
            }
        } catch (Exception e) {
            log.error("Kullanıcı kimlik doğrulaması yapılamadı: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
