package com.ifpr.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ifpr.backend.model.Usuario;
import com.ifpr.backend.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;
    
    public Usuario inserir(Usuario usuario){
        return repository.save(usuario);
    }

    public List<Usuario> listarTodos(){
        return repository.findAll();
    }

    public Usuario buscarPorId(Long id){
        Usuario usuario = repository.findById(id).orElseThrow(()
                ->new RuntimeException("Usuário não encontrado!!"));
        return usuario;
    }

    public void remover(Long id){
        Usuario usuario = buscarPorId(id);
        repository.delete(usuario);
    }

    public Usuario alterar(Usuario usuario){
        Usuario usuarioDB = buscarPorId(usuario.getId());
        usuarioDB.setNome(usuario.getNome());
        usuarioDB.setEmail(usuario.getEmail());
        return repository.save(usuarioDB);
    }
}
