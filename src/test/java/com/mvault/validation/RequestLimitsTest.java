package com.mvault.validation;

import com.mvault.dto.request.*;
import com.mvault.model.TipoMovimentacao;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class RequestLimitsTest {
    private static ValidatorFactory factory;
    @BeforeAll static void setup() { factory = Validation.buildDefaultValidatorFactory(); }
    @AfterAll static void close() { factory.close(); }

    @Test void nomeProduto() {
        assertTrue(factory.getValidator().validate(produto(150)).isEmpty());
        assertFalse(factory.getValidator().validate(produto(151)).isEmpty());
    }
    @Test void descricaoCategoria() {
        assertTrue(factory.getValidator().validate(new CategoriaRequest("Categoria", "a".repeat(255))).isEmpty());
        assertFalse(factory.getValidator().validate(new CategoriaRequest("Categoria", "a".repeat(256))).isEmpty());
    }
    @Test void observacaoMovimentacao() {
        assertTrue(factory.getValidator().validate(movimentacao(255)).isEmpty());
        assertFalse(factory.getValidator().validate(movimentacao(256)).isEmpty());
    }
    private ProdutoRequest produto(int length) {
        return new ProdutoRequest("a".repeat(length), "descricao", BigDecimal.ONE, 0, 1L, 1L);
    }
    private MovimentacaoRequest movimentacao(int length) {
        return new MovimentacaoRequest(TipoMovimentacao.ENTRADA, 1, 1L, "a".repeat(length));
    }
}
