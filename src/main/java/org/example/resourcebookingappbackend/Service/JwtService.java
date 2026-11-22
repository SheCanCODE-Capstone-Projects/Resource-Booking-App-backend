package org.example.resourcebookingappbackend.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {


    public final String secretKey;

    public JwtService(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    public String generateToken(String username , String role) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts
                .builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + 1000 * 60 * 60 * 24 * 90))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();

    }


    public String extractUsername(String token) {
        return  Jwts
                .parser()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenExpired(String token) {
        return  Jwts
                .parser()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getBody()
                .getExpiration()
                .before(new Date());

    }

    public boolean isTokenValid(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }
}
