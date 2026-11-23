package com.mvault.infra;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.mvault.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.Duration;

@Service
public class TokenService {
    private final String secret;
    private final Clock clock;

    @Autowired
    public TokenService(@Value("${api.security.token.secret}") String secret) {
        this(secret, Clock.systemUTC());
    }

    TokenService(String secret, Clock clock) {
        this.secret = secret;
        this.clock = clock;
    }

    public String gerarToken(Usuario usuario) {
        try {
            return JWT.create()
                    .withIssuer("MVault API")
                    .withSubject(usuario.getLogin())
                    .withExpiresAt(clock.instant().plus(Duration.ofHours(2)))
                    .sign(Algorithm.HMAC256(secret));
        } catch (JWTCreationException exception) {
            throw new IllegalStateException("Erro ao gerar token JWT", exception);
        }
    }

    public String getSubject(String tokenJWT) {
        try {
            String subject = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("MVault API").build().verify(tokenJWT).getSubject();
            if (subject == null || subject.isBlank()) {
                throw new BadCredentialsException("Token JWT sem usuário");
            }
            return subject;
        } catch (JWTVerificationException exception) {
            throw new BadCredentialsException("Token JWT inválido ou expirado", exception);
        }
    }
}
