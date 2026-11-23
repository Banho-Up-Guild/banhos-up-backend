package com.github.banhosup.security;

import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);

                // mesma chave que o JwtService usa para assinar
                byte[] key = MessageDigest.getInstance("SHA-256")
                        .digest(jwtSecret.getBytes(StandardCharsets.UTF_8));

                SignedJWT jwt = SignedJWT.parse(token);

                boolean valid = jwt.verify(new MACVerifier(key))
                        && jwt.getJWTClaimsSet().getExpirationTime().after(new Date());

                if (valid) {
                    String email = jwt.getJWTClaimsSet().getSubject();
                    List<String> roles = jwt.getJWTClaimsSet().getStringListClaim("roles");
                    List<SimpleGrantedAuthority> authorities =
                            roles.stream().map(SimpleGrantedAuthority::new).toList();

                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(email, null, authorities));
                }
            } catch (Exception e) {
                // token inválido: segue sem identificar ninguém
            }
        }

        filterChain.doFilter(request, response);
    }
}