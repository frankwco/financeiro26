package com.ifpr.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import com.ifpr.backend.model.Perfil;
import com.ifpr.backend.model.Usuario;
import com.ifpr.backend.model.UsuarioPerfil;
import com.ifpr.backend.repository.PerfilRepository;
import com.ifpr.backend.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private EnvioEmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario inserir(Usuario usuario) {
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
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

        // [LAB] proposital: qualquer usuário autenticado pode enviar "usuarioPerfil"
        // no corpo do PUT e se autoatribuir qualquer perfil (inclusive ADMIN), sem
        // nenhuma checagem de que quem está chamando já é administrador.
        if (usuario.getUsuarioPerfil() != null) {
            List<UsuarioPerfil> perfis = usuario.getUsuarioPerfil().stream()
                    .map(up -> {
                        Perfil perfil = perfilRepository.findById(up.getPerfil().getId())
                                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
                        UsuarioPerfil novo = new UsuarioPerfil();
                        novo.setPerfil(perfil);
                        return novo;
                    })
                    .collect(Collectors.toList());
            usuarioDB.setUsuarioPerfil(perfis);
        }

        return repository.save(usuarioDB);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
