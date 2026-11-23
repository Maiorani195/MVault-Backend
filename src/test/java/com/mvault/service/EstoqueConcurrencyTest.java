package com.mvault.service;

import com.mvault.dto.request.MovimentacaoRequest;
import com.mvault.exception.BusinessException;
import com.mvault.model.*;
import com.mvault.repository.*;
import com.mvault.support.IsolatedDatabaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@IsolatedDatabaseTest
class EstoqueConcurrencyTest {
    @Autowired MovimentacaoService service;
    @Autowired ProdutosRepository produtos;
    @Autowired CategoriaRepository categorias;
    @Autowired FornecedorRepository fornecedores;
    @Autowired MovimentacaoRepository movimentos;
    @Autowired PlatformTransactionManager manager;

    @Test
    void duasSaidasNaoPodemConsumirOMesmoSaldo() throws Exception {
        var tx = new TransactionTemplate(manager);
        Long id = tx.execute(status -> {
            Categoria categoria = categorias.save(new Categoria("Concorrencia", "", true));
            Fornecedor fornecedor = fornecedores.save(new Fornecedor(
                    "Teste concorrencia", "04252011000110", "", "", "", true));
            Produto produto = new Produto("Estoque concorrente", "", BigDecimal.ONE, 0, categoria, fornecedor);
            produto.adicionarEstoque(10);
            return produtos.save(produto).getId();
        });
        var firstChanged = new CountDownLatch(1);
        var releaseFirst = new CountDownLatch(1);
        var secondStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = executor.submit(() -> tx.executeWithoutResult(status -> {
                service.criar(new MovimentacaoRequest(TipoMovimentacao.SAIDA, 7, id, "primeira"));
                firstChanged.countDown();
                await(releaseFirst);
            }));
            assertTrue(firstChanged.await(10, TimeUnit.SECONDS), "Primeira transação não iniciou");
            Future<?> second = executor.submit(() -> {
                secondStarted.countDown();
                assertThrows(BusinessException.class, () ->
                        service.criar(new MovimentacaoRequest(TipoMovimentacao.SAIDA, 7, id, "segunda")));
            });
            assertTrue(secondStarted.await(10, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> second.get(300, TimeUnit.MILLISECONDS),
                    "A segunda saída deve aguardar a transação que detém o saldo");
            releaseFirst.countDown();
            first.get(15, TimeUnit.SECONDS);
            second.get(15, TimeUnit.SECONDS);
            assertEquals(3, produtos.findById(id).orElseThrow().getQuantidadeEstoque());
            long count = tx.execute(status -> movimentos.findAll().stream()
                    .filter(m -> m.getProduto().getId().equals(id)).count());
            assertEquals(1L, count);
        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS));
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Timeout da transação de teste");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }
}
