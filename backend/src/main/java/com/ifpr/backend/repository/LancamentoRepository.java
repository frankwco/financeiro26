package com.ifpr.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ifpr.backend.model.Lancamento;
import com.ifpr.backend.model.Usuario;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    List<Lancamento> findByUsuario(Usuario usuario);

}
