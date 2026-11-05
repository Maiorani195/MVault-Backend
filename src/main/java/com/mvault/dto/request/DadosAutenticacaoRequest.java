package com.mvault.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DadosAutenticacaoRequest(

        @NotBlank(message = "O login é obrigatório")
        String login,

        @NotBlank(message = "A senha é obrigatória")
        String senha
) {
}
