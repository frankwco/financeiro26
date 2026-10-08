package com.ifpr.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ifpr.backend.service.UsuarioService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class JwtFiltroAutenticacao extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioService usuarioService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            try {
                username = jwtService.extractUsername(token);
            } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
                // Token expirado/invalido: segue sem autenticar (login e refresh continuam acessiveis)
                logger.debug("Token JWT ignorado: " + e.getMessage());
            }
        }

        // [SSE] EventSource do navegador não consegue enviar o header Authorization,
        // então para o stream (e só para ele) aceitamos o token na query string.
        if (token == null && request.getRequestURI().endsWith("/lancamento/stream")) {
            token = request.getParameter("token");
            try {
                username = token != null ? jwtService.extractUsername(token) : null;
            } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
                logger.debug("Token JWT (stream) ignorado: " + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = usuarioService.loadUserByUsername(username);
            if (jwtService.validateToken(token, userDetails.getUsername())) {
                var authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
}
