package com.ifpr.backend.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifpr.backend.dto.AutenticacaoResultado;
import com.ifpr.backend.dto.UsuarioAutenticacaoDTO;
import com.ifpr.backend.dto.UsuarioRequisicaoDTO;
import com.ifpr.backend.service.AutenticacaoService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/autenticacao")
@CrossOrigin
public class AutenticacaoController {

    private static final String COOKIE_REFRESH_TOKEN = "refreshToken";

    @Autowired
    private AutenticacaoService autenticacaoService;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    @PostMapping("/login")
    public ResponseEntity<UsuarioAutenticacaoDTO> login(@RequestBody UsuarioRequisicaoDTO usuario,
            HttpServletResponse response) {
        AutenticacaoResultado resultado = autenticacaoService.autenticar(usuario);
        adicionarCookieRefreshToken(response, resultado.refreshToken());
        return ResponseEntity.ok(resultado.dados());
    }

    @PostMapping("/refresh")
    public ResponseEntity<UsuarioAutenticacaoDTO> refresh(
            @CookieValue(name = COOKIE_REFRESH_TOKEN, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            AutenticacaoResultado resultado = autenticacaoService.renovar(refreshToken);
            adicionarCookieRefreshToken(response, resultado.refreshToken());
            return ResponseEntity.ok(resultado.dados());
        } catch (RuntimeException ex) {
            removerCookieRefreshToken(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = COOKIE_REFRESH_TOKEN, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null) {
            autenticacaoService.logout(refreshToken);
        }
        removerCookieRefreshToken(response);
        return ResponseEntity.noContent().build();
    }

    private void adicionarCookieRefreshToken(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_REFRESH_TOKEN, refreshToken)
                .httpOnly(true)
                .secure(false) // trocar para true em produção, atrás de HTTPS
                .sameSite("Lax")
                .path("/autenticacao")
                .maxAge(Duration.ofMillis(refreshExpiration))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void removerCookieRefreshToken(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_REFRESH_TOKEN, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/autenticacao")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
