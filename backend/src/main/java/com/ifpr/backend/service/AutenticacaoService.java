package com.ifpr.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.ifpr.backend.dto.AutenticacaoResultado;
import com.ifpr.backend.dto.UsuarioAutenticacaoDTO;
import com.ifpr.backend.dto.UsuarioRequisicaoDTO;
import com.ifpr.backend.model.RefreshToken;
import com.ifpr.backend.model.Usuario;
import com.ifpr.backend.repository.UsuarioRepository;
import com.ifpr.backend.security.JwtService;
import com.ifpr.backend.security.RefreshTokenService;

@Service
public class AutenticacaoService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public AutenticacaoResultado autenticar(UsuarioRequisicaoDTO usuario) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usuario.getEmail(), usuario.getSenha()));

        Usuario usuarioBanco = usuarioRepository.findByEmail(usuario.getEmail()).get();

        RefreshToken refreshToken = refreshTokenService.criar(usuarioBanco);

        return montarResultado(usuarioBanco, authentication.getName(), refreshToken);
    }

    public AutenticacaoResultado renovar(String refreshTokenRecebido) {
        RefreshToken refreshTokenValido = refreshTokenService.validar(refreshTokenRecebido);
        Usuario usuarioBanco = refreshTokenValido.getUsuario();

        RefreshToken refreshTokenNovo = refreshTokenService.rotacionar(refreshTokenValido);

        return montarResultado(usuarioBanco, usuarioBanco.getUsername(), refreshTokenNovo);
    }

    public void logout(String refreshTokenRecebido) {
        refreshTokenService.revogar(refreshTokenRecebido);
    }

    private AutenticacaoResultado montarResultado(Usuario usuarioBanco, String username, RefreshToken refreshToken) {
        UsuarioAutenticacaoDTO autenticacaoDTO = new UsuarioAutenticacaoDTO();
        autenticacaoDTO.setEmail(usuarioBanco.getEmail());
        autenticacaoDTO.setNome(usuarioBanco.getNome());
        autenticacaoDTO.setToken(jwtService.generateToken(username));

        return new AutenticacaoResultado(autenticacaoDTO, refreshToken.getToken());
    }
}
