package com.github.banhosup.security;

import com.github.banhosup.model.entities.user.UserEntity;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-seconds:3600}")
    private long expirationSeconds;

    // Monta as claims (os dados do token) e delega a assinatura para jwtEncode()
    public String generateToken(UserEntity user) {
        // Ex.: ["ROLE_CUSTOMER"]
        List<String> roles = List.of("ROLE_" + user.getRole().name());

        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(expirationSeconds);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("banho-up-api")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiration))
                .claim("roles", roles)
                .build();

        return jwtEncode(claims);
    }

    // Validade em milissegundos, para o LoginResponse
    public long getExpirationMs() {
        return expirationSeconds * 1000;
    }

    // Assina as claims e devolve o texto "header.payload.signature"
    private String jwtEncode(JWTClaimsSet claims) {
        try {
            // 1. Transforma o segredo (qualquer tamanho) numa chave de 256 bits com SHA-256
            byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                    .digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
            MACSigner signer = new MACSigner(keyBytes);

            // 2. Junta o cabeçalho (algoritmo HS256) com o conteúdo (claims)
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);

            // 3. Assina com a chave
            jwt.sign(signer);

            // 4. Converte para o texto final
            return jwt.serialize();

        } catch (JOSEException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao assinar o token JWT", e);
        }
    }
}