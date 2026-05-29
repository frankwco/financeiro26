package com.ifpr.backend.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Entity
@Data
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Nome Obrigatório")
    @Size(min = 10, message = "Insira o nome completo")
    private String nome;
    @Email(message = "Insira um email válido")
    private String email;
    private String senha;
    @OneToMany(mappedBy = "usuario", 
    cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(value = AccessLevel.NONE)
    private List<UsuarioPerfil> usuarioPerfil;

    public void setUsuarioPerfil(List<UsuarioPerfil> usuariosPerfis){
        if(usuariosPerfis!=null){
            for(UsuarioPerfil u:usuariosPerfis){
                u.setUsuario(this);
            }
        }
        this.usuarioPerfil = usuariosPerfis;
    }
}
