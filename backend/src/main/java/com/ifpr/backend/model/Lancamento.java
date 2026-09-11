package com.ifpr.backend.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
public class Lancamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Descrição obrigatória")
    private String descricao;

    // [LAB] proposital: sem @Positive/@DecimalMin. A regra "valor deve ser
    // positivo" e o limite de saldo para despesas existem só no formulário React.
    private Double valor;

    // [LAB] proposital: String livre em vez de enum — nada barra um valor fora
    // de RECEITA/DESPESA vindo direto da API.
    private String tipo;

    private LocalDate data;

    private String comprovantePath;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    @JsonIgnoreProperties({ "usuarioPerfil" })
    private Usuario usuario;
}
