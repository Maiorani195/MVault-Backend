package com.mvault.infra;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.mvault.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.authentication.BadCredentialsException;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {
    private static final String SECRET = "test-only-signing-key-not-for-production";
    private final Usuario usuario = new Usuario(1L, "teste", "hash-test");

    @ParameterizedTest
    @ValueSource(strings = {"UTC", "America/Sao_Paulo", "Asia/Tokyo"})
    void expiraEmDuasHorasIndependentementeDoFuso(String zone) {
        Instant now = Instant.parse("2026-11-23T12:00:00Z");
        TokenService service = new TokenService(SECRET, Clock.fixed(now, ZoneId.of(zone)));
        assertEquals(now.plus(Duration.ofHours(2)),
                JWT.decode(service.gerarToken(usuario)).getExpiresAtAsInstant());
    }

    @Test
    void aceitaTokenValido() {
        TokenService service = new TokenService(SECRET);
        assertEquals("teste", service.getSubject(service.gerarToken(usuario)));
    }

    @Test
    void rejeitaTokenExpirado() {
        String token = JWT.create().withIssuer("MVault API").withSubject("teste")
                .withExpiresAt(Instant.EPOCH).sign(Algorithm.HMAC256(SECRET));
        assertThrows(BadCredentialsException.class, () -> new TokenService(SECRET).getSubject(token));
    }

    @Test
    void rejeitaAssinaturaInvalida() {
        String token = new TokenService("other-test-key").gerarToken(usuario);
        assertThrows(BadCredentialsException.class, () -> new TokenService(SECRET).getSubject(token));
    }

    @Test
    void rejeitaTokenSemUsuario() {
        String token = JWT.create().withIssuer("MVault API")
                .withExpiresAt(Instant.now().plusSeconds(60)).sign(Algorithm.HMAC256(SECRET));
        assertThrows(BadCredentialsException.class, () -> new TokenService(SECRET).getSubject(token));
    }
}
