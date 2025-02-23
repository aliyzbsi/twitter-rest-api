package com.twitter.twitter_rest_api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token.expiration}")
    private int accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token.expiration}")
    private int refreshTokenExpirationMs;

    private Key getSigningKey(){
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, accessTokenExpirationMs, SignatureAlgorithm.HS256);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, refreshTokenExpirationMs, SignatureAlgorithm.HS512);
    }

    public String generateToken(UserDetails userDetails,int expirationMs,SignatureAlgorithm algorithm){
        Map<String,Object> claims=new HashMap<>();
        claims.put("type",algorithm==SignatureAlgorithm.HS256?"ACCESS":"REFRESH");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expirationMs))
                .signWith(getSigningKey(), algorithm)
                .compact();
    }

    public boolean isRefreshToken(String token){
        Claims claims=extractClaims(token);
        return "REFRESH".equals(claims.get("type"));
    }
    public Claims extractClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUserNameFromJwtToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        }catch (SecurityException | MalformedJwtException | ExpiredJwtException |
                UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String parseJwt(HttpServletRequest request){
        String headerAuth=request.getHeader("Authorization");

        if(StringUtils.hasText(headerAuth)&&headerAuth.startsWith("Bearer ")){
            return headerAuth.substring(7);
        }
        return null;
    }

}
