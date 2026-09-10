package com.ifpr.backend.security;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ifpr.backend.model.RefreshToken;
import com.ifpr.backend.model.Usuario;
import com.ifpr.backend.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh.expiration}")
    private Long expiration;

    @Autowired
    private RefreshTokenRepository repository;

    public RefreshToken criar(Usuario usuario) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsuario(usuario);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setDataExpiracao(LocalDateTime.now().plus(Duration.ofMillis(expiration)));
        refreshToken.setRevogado(false);
        return repository.save(refreshToken);
    }

    public RefreshToken validar(String token) {
        RefreshToken refreshToken = repository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token inválido"));

        if (refreshToken.isRevogado() || refreshToken.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expirado ou revogado");
        }

        return refreshToken;
    }

    public RefreshToken rotacionar(RefreshToken tokenAntigo) {
        tokenAntigo.setRevogado(true);
        repository.save(tokenAntigo);
        return criar(tokenAntigo.getUsuario());
    }

    public void revogar(String token) {
        repository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevogado(true);
            repository.save(refreshToken);
        });
    }
}
