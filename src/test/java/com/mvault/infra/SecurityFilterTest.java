package com.mvault.infra;

import com.mvault.model.Usuario;
import com.mvault.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityFilterTest {
    private final TokenService tokens = mock(TokenService.class);
    private final UsuarioRepository users = mock(UsuarioRepository.class);
    private final SecurityFilter filter = new SecurityFilter(tokens, users);
    private final FilterChain chain = mock(FilterChain.class);
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void semTokenContinuaSemAutenticacao() throws Exception {
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        verifyNoInteractions(tokens, users);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bearer ", "Basic abc", "abc", "Bearer"})
    void rejeitaCabecalhoInvalido(String header) throws Exception {
        request.addHeader("Authorization", header);
        filter.doFilter(request, response, chain);
        assertUnauthorized();
        verifyNoInteractions(tokens, users);
    }

    @Test
    void tokenInvalidoRetorna401() throws Exception {
        request.addHeader("Authorization", "Bearer invalid");
        when(tokens.getSubject("invalid")).thenThrow(new BadCredentialsException("expired"));
        filter.doFilter(request, response, chain);
        assertUnauthorized();
    }

    @Test
    void usuarioRemovidoRetorna401() throws Exception {
        request.addHeader("Authorization", "Bearer valid");
        when(tokens.getSubject("valid")).thenReturn("removed");
        filter.doFilter(request, response, chain);
        assertUnauthorized();
    }

    @Test
    void tokenValidoAutentica() throws Exception {
        request.addHeader("Authorization", "bearer valid");
        Usuario usuario = new Usuario(1L, "teste", "hash-test");
        when(tokens.getSubject("valid")).thenReturn("teste");
        when(users.findByLogin("teste")).thenReturn(usuario);
        filter.doFilter(request, response, chain);
        assertSame(usuario, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(chain).doFilter(request, response);
    }

    @Test
    void naoConverteFalhaDaAplicacaoEm401() throws Exception {
        doThrow(new ServletException("application failure")).when(chain).doFilter(request, response);
        assertThrows(ServletException.class, () -> filter.doFilter(request, response, chain));
        assertNotEquals(401, response.getStatus());
    }

    private void assertUnauthorized() {
        assertEquals(401, response.getStatus());
        assertEquals("Bearer", response.getHeader("WWW-Authenticate"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(chain);
    }
}
