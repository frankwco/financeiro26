package com.ifpr.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.ifpr.backend.dto.UsuarioAutenticacaoDTO;
import com.ifpr.backend.dto.UsuarioRequisicaoDTO;
import com.ifpr.backend.model.Usuario;
import com.ifpr.backend.repository.UsuarioRepository;
import com.ifpr.backend.security.JwtService;

@Service
public class AutenticacaoService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public UsuarioAutenticacaoDTO autenticar(UsuarioRequisicaoDTO usuario) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usuario.getEmail(), usuario.getSenha()));

        Usuario usuarioBanco = usuarioRepository.findByEmail(usuario.getEmail()).get();

        UsuarioAutenticacaoDTO autenticacaoDTO = new UsuarioAutenticacaoDTO();
        autenticacaoDTO.setEmail(usuarioBanco.getEmail());
        autenticacaoDTO.setNome(usuarioBanco.getNome());
        autenticacaoDTO.setToken(jwtService.generateToken(authentication.getName()));

        return autenticacaoDTO;
    }
}
