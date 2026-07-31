package com.ifpr.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import com.ifpr.backend.model.Usuario;
import com.ifpr.backend.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private EnvioEmailService emailService;

    public Usuario inserir(Usuario usuario) {
        Usuario usuarioBanco = repository.save(usuario);
        //emailService.enviarEmail(usuario.getEmail(), "Sucesso", "Cadastro realizado com sucesso!!");
        Context context = new Context();
        context.setVariable("nome", usuario.getNome());
        //context.setVariable("email", usuario.getEmail());

        emailService.enviarEmailTemplate(usuario.getEmail(), "Sucesso", "novoCadastro", context);

        return usuarioBanco;
    }

    public List<Usuario> listarTodos() {
        return repository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        Usuario usuario = repository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado!!"));
        return usuario;
    }

    public void remover(Long id) {
        Usuario usuario = buscarPorId(id);
        repository.delete(usuario);
    }

    public Usuario alterar(Usuario usuario) {
        Usuario usuarioDB = buscarPorId(usuario.getId());
        usuarioDB.setNome(usuario.getNome());
        usuarioDB.setEmail(usuario.getEmail());
        return repository.save(usuarioDB);
    }
}
