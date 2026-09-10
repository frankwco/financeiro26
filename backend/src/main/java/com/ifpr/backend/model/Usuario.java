package com.ifpr.backend.model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Nome Obrigatório")
    @Size(min = 10, message = "Insira o nome completo")
    private String nome;
    @Email(message = "Insira um email válido")
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;
    @OneToMany(mappedBy = "usuario",
    cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
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

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (usuarioPerfil == null) {
            return List.of();
        }
        return usuarioPerfil.stream()
                .map(up -> new SimpleGrantedAuthority(up.getPerfil().getDescricao()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
