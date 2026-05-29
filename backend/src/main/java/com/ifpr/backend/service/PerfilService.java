package com.ifpr.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ifpr.backend.model.Perfil;
import com.ifpr.backend.model.Usuario;
import com.ifpr.backend.repository.PerfilRepository;
import com.ifpr.backend.repository.UsuarioRepository;

@Service
public class PerfilService {

    @Autowired
    private PerfilRepository repository;
    
    public Perfil inserir(Perfil perfil){
        return repository.save(perfil);
    }

    public List<Perfil> listarTodos(){
        return repository.findAll();
    }

    public Perfil buscarPorId(UUID id){
        Perfil perfil = repository.findById(id).orElseThrow(()
                ->new RuntimeException("Perfil não encontrado!!"));
        return perfil;
    }

    public void remover(UUID id){
        Perfil usuario = buscarPorId(id);
        repository.delete(usuario);
    }

    public Perfil alterar(Perfil perfil){
        Perfil perfilDB = buscarPorId(perfil.getId());
        perfilDB.setDescricao(perfil.getDescricao());
        return repository.save(perfilDB);
    }
}
