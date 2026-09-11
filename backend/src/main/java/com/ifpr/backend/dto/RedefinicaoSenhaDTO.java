package com.ifpr.backend.dto;

import lombok.Data;

@Data
public class RedefinicaoSenhaDTO {
    private String email;
    private String token;
    private String novaSenha;
}
