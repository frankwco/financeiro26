package com.ifpr.backend.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import com.ifpr.backend.dto.RecuperacaoSenhaDTO;
import com.ifpr.backend.model.TokenRecuperacaoSenha;
import com.ifpr.backend.model.Usuario;
import com.ifpr.backend.repository.TokenRecuperacaoSenhaRepository;
import com.ifpr.backend.repository.UsuarioRepository;

@Service
public class RecuperacaoSenhaService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenRecuperacaoSenhaRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EnvioEmailService emailService;

    public RecuperacaoSenhaDTO solicitar(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // [LAB] proposital: token numérico curto, gerado com java.util.Random (não
        // SecureRandom) — só 900.000 combinações possíveis, viável de forçar por
        // tentativa e erro (ainda mais sem limite de tentativas, ver força bruta).
        String token = String.valueOf(new Random().nextInt(900000) + 100000);

        TokenRecuperacaoSenha registro = new TokenRecuperacaoSenha();
        registro.setUsuario(usuario);
        registro.setToken(token);
        registro.setDataExpiracao(LocalDateTime.now().plusHours(1));
        registro.setUsado(false);
        tokenRepository.save(registro);

        Context context = new Context();
        context.setVariable("token", token);
        emailService.enviarEmailTemplate(usuario.getEmail(), "Recuperação de senha", "recuperacaoSenha", context);

        return new RecuperacaoSenhaDTO("Se o e-mail existir, um código foi enviado.", token);
    }

    public void redefinir(String email, String tokenRecebido, String novaSenha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        TokenRecuperacaoSenha registro = tokenRepository.findByToken(tokenRecebido)
                .filter(t -> t.getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new RuntimeException("Código inválido"));

        if (registro.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Código expirado");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        // [LAB] proposital: o registro nunca é marcado como usado nem removido —
        // o mesmo código continua valendo para redefinir a senha de novo, quantas
        // vezes quiser, até a expiração de 1 hora.
    }
}
