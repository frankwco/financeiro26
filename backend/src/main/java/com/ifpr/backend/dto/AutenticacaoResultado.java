package com.ifpr.backend.dto;

public record AutenticacaoResultado(UsuarioAutenticacaoDTO dados, String refreshToken) {
}
