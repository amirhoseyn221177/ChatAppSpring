package com.example.websocketdemo.Security;

import com.example.websocketdemo.Services.CustomUserServices;
import com.example.websocketdemo.model.ChatUser;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import java.util.List;
import java.util.stream.Collectors;

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
          System.out.println(37);
          String jwt =getJwtFromRequest(request);
          System.out.println(jwt);
          if(StringUtils.hasText(jwt)&&tokenValidator.validateToken(jwt)){
              String userId=tokenValidator.GetIdFromToken(jwt);
              ChatUser chatUser = customUserServices.loadByID(userId);
              Claims claims =  tokenValidator.getClaimsFromToken(jwt);
              List<String> roles=claims.get("roles",List.class);
              System.out.println(roles);
              List<SimpleGrantedAuthority> authorities =roles.stream().map(role->new SimpleGrantedAuthority(role)).collect(Collectors.toList());
              UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(
                      chatUser.getUsername(),null, authorities);
              authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
              SecurityContextHolder.getContext().setAuthentication(authenticationToken);
              System.out.println(SecurityContextHolder.getContext().getAuthentication());
          }
      }catch (Exception e){
          System.out.println(e.getMessage());
      }
       filterChain.doFilter(request,response);

    }

    private String getJwtFromRequest(HttpServletRequest req){
        System.out.println(req.getHeader("Authorization"));
        String bearerToken = req.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken)&&bearerToken.startsWith("bearer")){
            return bearerToken.substring(7);
        }
        return null;
    }
}
