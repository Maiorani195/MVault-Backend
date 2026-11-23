package com.mvault.service;

import com.mvault.model.Usuario;
import com.mvault.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutenticacaoServiceTest {
    @Test
    void usuarioAusenteLancaExcecaoEsperada() {
        var repository = mock(UsuarioRepository.class);
        var service = new AutenticacaoService(repository);
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("ausente"));
    }

    @Test
    void usuarioExistenteEhRetornado() {
        var repository = mock(UsuarioRepository.class);
        var usuario = new Usuario(1L, "teste", "hash-test");
        when(repository.findByLogin("teste")).thenReturn(usuario);
        assertSame(usuario, new AutenticacaoService(repository).loadUserByUsername("teste"));
    }
}
