package com.example.websocketdemo.Security;

import com.example.websocketdemo.Services.CustomUserServices;
import com.example.websocketdemo.model.ChatUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenValidator tokenValidator;
    private final CustomUserServices customUserServices;

    public JwtAuthenticationFilter(TokenValidator tokenValidator, CustomUserServices customUserServices) {
        this.tokenValidator = tokenValidator;
        this.customUserServices = customUserServices;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
      try {
          String jwt =getJwtFromRequest(request);
          if(tokenValidator.validateToken(jwt)){
              String userId=tokenValidator.GetIdFromToken(jwt);
              ChatUser chatUser = customUserServices.loadByID(userId);
              UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(
                      chatUser,null, Collections.emptyList());
              authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
              SecurityContextHolder.getContext().setAuthentication(authenticationToken);
          }
      }catch (Exception e){
          System.out.println(e.getLocalizedMessage());
      }
       filterChain.doFilter(request,response);

    }

    private String getJwtFromRequest(HttpServletRequest req){
        String bearerToken = req.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken)&&bearerToken.startsWith("bearer")){
            return bearerToken.substring(7,bearerToken.length());
        }
        return null;
    }
}
