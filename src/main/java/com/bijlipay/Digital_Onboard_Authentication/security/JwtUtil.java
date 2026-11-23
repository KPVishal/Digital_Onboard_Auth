package com.bijlipay.Digital_Onboard_Authentication.security;

import com.bijlipay.Digital_Onboard_Authentication.dto.PasswordToken;
import io.jsonwebtoken.*;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Key;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${app.jwtExpirationInMs}")
    private long jwtExpirationInMs;

    String AUTHORITIES_KEY = "authorities-key";

    @Value("${app.secret}")
    private String secret;

    //2.Read Claims
    public Claims getClaim(String token){

        return Jwts.parser()
                .setSigningKey(secret.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    //3.Read Exp date
    public Date getExpDate(String token){
        return getClaim(token).getExpiration();
    }




    //4.Read subject/username
    public String getUsername(String token){
        return getClaim(token).getSubject();
    }

    //5.Validate Exp Date
    public boolean isTokenExp(String token){
        Date expdate=getExpDate(token);
        return expdate.before(new Date(System.currentTimeMillis()));
    }

    //6.Validate username in token and database,expDate
    public boolean validateToken(String token,String username){
        String tokenUserName=getUsername(token);
        return (username.equals(tokenUserName) && !isTokenExp(token));
    }


//==============================================================================================

    //1.Generate Token
    public String generateToken(String subject){
        return Jwts.builder()
                .setSubject(subject)
                .setIssuer("Vaishu")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ TimeUnit.MINUTES.toMillis(1440)))
                .signWith(SignatureAlgorithm.HS512,secret.getBytes())
                .compact();

    }


    public String generatePasswordToken(PasswordToken token) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(token.getUserId()));
        claims.put("userId", token.getUserId());
        claims.put("used", token.isUsed());
        claims.put("oneTimePassword", token.getOneTimePassword());
        return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, secret).setIssuedAt(new Date(System.currentTimeMillis())).setExpiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1440))).compact();
    }

    public PasswordToken validatePasswordToken(String token) throws UnsupportedEncodingException {
        token = URLDecoder.decode(token, "UTF-8");
        PasswordToken passwordToken;
        try {
            Claims body = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            if (body.get("userId") != null && !body.get("userId").equals("") && body.getExpiration().getTime() > System.currentTimeMillis()) {
                passwordToken = new PasswordToken();
                passwordToken.setOneTimePassword((Integer) body.get("oneTimePassword"));
                passwordToken.setUserId((Integer) body.get("userId"));
                passwordToken.setUsed((Boolean) body.get("used"));
            } else {
                throw new RuntimeException("Invalid token!");
            }

        } catch (Exception e) {
            System.out.println(e);
            throw e;
        }
        return passwordToken;
    }
}
