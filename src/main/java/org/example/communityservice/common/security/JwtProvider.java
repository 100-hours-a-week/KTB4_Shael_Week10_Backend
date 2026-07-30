package org.example.communityservice.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtProperties jwtProperties;
    private SecretKey key;

    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private String createToken(String type, Long userId, long expSeconds){
        Instant now = Instant.now();

        return Jwts.builder().subject(String.valueOf(userId))
                .id(UUID.randomUUID().toString())
                .claim("typ", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expSeconds)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String createAccessToken(Long userId){
        return createToken("access", userId, jwtProperties.getAccessTokenExpSeconds());
    }

    public String createRefreshToken(Long userId){
        return createToken("refresh", userId, jwtProperties.getRefreshTokenExpSeconds());
    }

    public Jws<Claims> parse(String token){
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }

    public Long getAccessTokenUserId(String token) {
        return Long.valueOf(getClaimsByType(token, "access").getSubject());
    }

    public Long getRefreshTokenUserId(String token) {
        return Long.valueOf(getClaimsByType(token, "refresh").getSubject());
    }

    private Claims getClaimsByType(String token, String expectedType) {
        Claims claims = parse(token).getPayload();
        String tokenType = claims.get("typ", String.class);

        if (!expectedType.equals(tokenType)) {
            throw new IllegalArgumentException("invalid_token_type");
        }

        return claims;
    }

    public Long getAccessTokenValidityInMilliseconds(){
        return jwtProperties.getAccessTokenExpSeconds() * 1000;
    }

    public long getRefreshTokenValidityInSeconds() {
        return jwtProperties.getRefreshTokenExpSeconds();
    }
}
