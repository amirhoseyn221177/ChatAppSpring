package com.example.websocketdemo.Security;

import com.example.websocketdemo.model.ChatUser;
import com.example.websocketdemo.model.Role;
import io.jsonwebtoken.*;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@PropertySource("classpath:application.properties")
public class TokenValidator {
    private final Environment env;

    public TokenValidator(Environment env) {
        this.env = env;
    }

    public String generateToken(Authentication authentication){
        ChatUser chatUser=(ChatUser) authentication.getPrincipal();
        Date now = new Date(System.currentTimeMillis());
        Date expiry = new Date(now.getTime()+100000000);
        String userId = chatUser.getId();
        List<String> authorities= chatUser.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        Map<String,Object> claims= new HashMap<>();
        claims.put("id",userId);
        claims.put("username",chatUser.getUsername());
        claims.put("roles",authorities);
        return Jwts.builder()
                .setSubject(userId)
                .setClaims(claims)
                .setExpiration(expiry)
                .setIssuedAt(now)
                .signWith(SignatureAlgorithm.HS512,env.getProperty("signature"))
                .compact();
    }


    public boolean validateToken(String token){
        try{
            Claims claims=Jwts.parser().setSigningKey(env.getProperty("signature")).parseClaimsJws(token).getBody();
            if(claims.getExpiration().compareTo(new Date(System.currentTimeMillis()))<0) throw new Exception("expired Token");
            return true;
        }catch (SignatureException e){
            System.out.println("Invalid JWT Signature");
        }catch (MalformedJwtException e){
            System.out.println("Invalid JWT token");
        }catch (ExpiredJwtException e){
            System.out.println("Expired JWT token");
        }catch (UnsupportedJwtException e){
            System.out.println("Unsupported JWT token");
        }catch (IllegalArgumentException e){
            System.out.println("JWT claims string is empty");
        }catch (Exception e){
            System.out.println(e.getLocalizedMessage());
        }
        return  false;
    }

    public String GetIdFromToken(String token){

        Claims claims = Jwts.parser().setSigningKey(env.getProperty("signature")).parseClaimsJws(token).getBody();
        return (String) claims.get("id");
    }

    public Claims getClaimsFromToken(String token){
       return  Jwts.parser().setSigningKey(env.getProperty("signature")).parseClaimsJws(token).getBody();
    }

    public String getJwtFromRequest(String bearerToken){
        if(StringUtils.hasText(bearerToken)&&bearerToken.startsWith("bearer")){
            return bearerToken.substring(7);
        }
        return null;
    }

}
