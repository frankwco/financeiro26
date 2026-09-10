package com.ifpr.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifpr.backend.dto.UsuarioAutenticacaoDTO;
import com.ifpr.backend.dto.UsuarioRequisicaoDTO;
import com.ifpr.backend.service.AutenticacaoService;

@RestController
@RequestMapping("/autenticacao")
@CrossOrigin
public class AutenticacaoController {

    @Autowired
    private AutenticacaoService autenticacaoService;

    @PostMapping("/login")
    public ResponseEntity<UsuarioAutenticacaoDTO> login(@RequestBody UsuarioRequisicaoDTO usuario) {
        return ResponseEntity.ok(autenticacaoService.autenticar(usuario));
    }
}
