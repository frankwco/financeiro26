package com.ifpr.backend.dto;

import lombok.Data;

@Data
public class UsuarioAutenticacaoDTO {
    private String nome;
    private String email;
    private String token;
}
