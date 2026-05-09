package core.utill;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static SecretKey secretKey = null;
    private final long accessTokenExpiration = 1000 * 60 * 60; // 1시간
    private final long refreshTokenExpiration = 1000 * 60 * 60 * 24 * 7; // 7일

    public JwtUtil(@Value("${jwt.access.secret.key}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Access Token 생성
    public String generateAccessToken(String memberId, String memberCode, String role, String coordinatorCode, String sessionToken) {
        return Jwts.builder()
                .subject(memberId)
                .claim("role", role)
                .claim("memberCode", memberCode)
                .claim("sessionToken", sessionToken)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(String memberId, String memberCode, String role, String coordinatorCode, String sessionToken) {
        return Jwts.builder()
                .subject(memberId)
                .claim("role", role)
                .claim("memberCode", memberCode)
                .claim("sessionToken", sessionToken)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    // Access Token 생성
    public String generateAdminAccessToken(String adminId, String adminCode, String role) {
        return Jwts.builder()
                .subject(adminId)
                .claim("role", role)
                .claim("adminCode", adminCode)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성
    public String generateAdminRefreshToken(String adminId, String adminCode, String role) {
        return Jwts.builder()
                .subject(adminId)
                .claim("role", role)
                .claim("adminCode", adminCode)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    // Token에서 memberId 추출
    public static String getMemberIdFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public static String getMemberCodeFromToken(String token) {
        return getClaims(token).get("memberCode", String.class);
    }

    // token에서 sessionToken 추출
    public static String getSessionTokenFromToken(String token) {
        return getClaims(token).get("sessionToken", String.class);
    }

    public static String getAdminCodeFromToken(String token) {
        return getClaims(token).get("adminCode", String.class);
    }

    // Token 유효성 검증
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            return false;
        }
    }

    public static Claims getClaims(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("JWT token is missing");
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }


}
