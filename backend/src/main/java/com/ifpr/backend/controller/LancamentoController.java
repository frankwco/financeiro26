package com.ifpr.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifpr.backend.model.Lancamento;
import com.ifpr.backend.service.LancamentoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/lancamento")
public class LancamentoController {

    @Autowired
    private LancamentoService service;

    @GetMapping
    public ResponseEntity<List<Lancamento>> listar() {
        return ResponseEntity.ok(service.listarMeusLancamentos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lancamento> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Lancamento> inserir(@RequestBody @Valid Lancamento lancamento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.inserir(lancamento));
    }

    @PutMapping
    public ResponseEntity<Lancamento> alterar(@RequestBody @Valid Lancamento lancamento) {
        return ResponseEntity.ok(service.alterar(lancamento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable("id") Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
