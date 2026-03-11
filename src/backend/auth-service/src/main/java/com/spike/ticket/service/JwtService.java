package com.spike.ticket.service;

import com.spike.ticket.entity.User;
import com.spike.ticket.utils.RsaKeyReader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final RsaKeyReader rsaKeyReader;

    // 1 ngày
    private static final Long EXPIRATION_TIME = 86400000L;

    public String generateToken(User user) {
        try {
            PrivateKey privateKey = rsaKeyReader.getPrivateKey();

            return Jwts.builder()
                    // nhét system role và userId
                    .setSubject(String.valueOf(user.getId()))
                    .claim("role",user.getRole().toString() )
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(privateKey, SignatureAlgorithm.RS256)
                    .compact();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo JWT: " + e.getMessage());
        }
    }
}
