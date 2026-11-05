package com.mvault.specification;

import com.mvault.model.Fornecedor;
import org.springframework.data.jpa.domain.Specification;

public class FornecedorSpecification {

    public static Specification<Fornecedor> comFiltro(String nome, String cnpj, Boolean ativo) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();


            if (ativo != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("ativo"), ativo));
            } else {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.isTrue(root.get("ativo")));
            }

            if (nome != null && !nome.isBlank()) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), "%" + nome.toLowerCase() + "%")
                );
            }

            if (cnpj != null && !cnpj.isBlank()) {
                String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(root.get("cnpj"), cnpjLimpo)
                );
            }

            return predicate;
        };
    }
}