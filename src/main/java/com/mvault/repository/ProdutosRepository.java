package com.mvault.repository;

import com.mvault.model.Produto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProdutosRepository extends JpaRepository<Produto, Long> , JpaSpecificationExecutor<Produto> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> findByIdParaAtualizacao(@Param("id") Long id);

    boolean existsByNomeIgnoreCase(String nome);


    boolean existsByNomeIgnoreCaseAndIdNot(String nome , Long id);
}
