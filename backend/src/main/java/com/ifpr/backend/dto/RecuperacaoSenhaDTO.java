package com.ifpr.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecuperacaoSenhaDTO {
    private String mensagem;
    // [LAB] proposital: o token deveria viajar só por e-mail. Devolvê-lo aqui na
    // resposta da API permite redefinir a senha de qualquer um sem acessar o e-mail dele.
    private String token;
}
