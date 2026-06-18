package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.service.UserDetailServiceImpl;
import com.ecommerce.project.security.service.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;




@Component
public class JwtUtils {
    // Getting JWt from header
    private static  final Logger logger = LoggerFactory.getLogger(JwtUtils.class );

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;
    @Value("${spring.ecom.app.jwtCookie}")
    private String jwtCookie;


    public String getJwtFromCookies(HttpServletRequest request){
        Cookie cookie = WebUtils.getCookie(request,jwtCookie);
        if(cookie != null ){

            return cookie.getValue();
        }else{
            return null;
        }
    }

    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal){
        String jwt = generateTokenfromUserName(userPrincipal.getUsername());
        ResponseCookie cookie = ResponseCookie.from(jwtCookie,jwt)
                .path("/api")
                .maxAge(24*60*60)
                .httpOnly(false)
                .build();
        return cookie;
    }
    public String generateTokenfromUserName(String username){
        return Jwts.builder().
                subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime() +jwtExpirationMs)))
                .signWith(key())
                .compact();
    }

    // Gertting username form jwt token(username from token)
    public String getUserNameFromJWTToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    // Generating signing key
    public Key key(){
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode("z9Qm8vL2kP7xR1sT5nY6cD3hJ8aB4uW9qM0eF2gH1kL3pR6tS8vN5xC7yZ0dA1b")
        );
//        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    // validate jwt token
    public boolean validateJwtToken(String authToken){
        try{

            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        }catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {} ", e.getMessage());

        }catch (ExpiredJwtException e){
            logger.error("JWT token is expired: {} ",e.getMessage());
        }catch(UnsupportedJwtException e){
            logger.error("JWT token is unsupported : {} ",e.getMessage());
        }catch(IllegalArgumentException e){
            logger.error("JWT claims string is empty: {} ",e.getMessage());
        }
        return false;
    }
}
