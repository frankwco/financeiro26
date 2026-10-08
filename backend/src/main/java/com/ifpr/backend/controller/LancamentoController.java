package com.ifpr.backend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.ifpr.backend.model.Lancamento;
import com.ifpr.backend.security.AuthUsuarioProvider;
import com.ifpr.backend.service.LancamentoEventosService;
import com.ifpr.backend.service.LancamentoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/lancamento")
public class LancamentoController {

    // [LAB] pasta relativa ao diretório de trabalho do backend (ex.: backend/uploads
    // quando rodado com "mvnw spring-boot:run" a partir de backend/).
    private static final String UPLOAD_DIR = "uploads";

    @Autowired
    private LancamentoService service;

    @Autowired
    private LancamentoEventosService eventos;

    @Autowired
    private AuthUsuarioProvider authUsuarioProvider;

    // [SSE] Conexão longa: o navegador abre e o servidor vai empurrando eventos.
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return eventos.registrar(authUsuarioProvider.getUsuarioAutenticado().getId());
    }

    @GetMapping
    public ResponseEntity<List<Lancamento>> listar() {
        return ResponseEntity.ok(service.listarMeusLancamentos());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Lancamento>> buscar(@RequestParam("descricao") String descricao) {
        return ResponseEntity.ok(service.buscarPorDescricao(descricao));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/todos")
    public ResponseEntity<List<Lancamento>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
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

    @PostMapping("/{id}/comprovante")
    public ResponseEntity<Lancamento> anexarComprovante(@PathVariable("id") Long id,
            @RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        Lancamento lancamento = service.buscarPorId(id);

        // [LAB] proposital: usa o nome de arquivo original enviado pelo cliente, sem
        // sanitizar nem validar extensão/tamanho/tipo — permite gravar fora da pasta
        // "uploads" se o nome contiver "../".
        String nomeArquivo = arquivo.getOriginalFilename();
        Path destino = Paths.get(UPLOAD_DIR).resolve(nomeArquivo);
        Files.createDirectories(destino.getParent());
        arquivo.transferTo(destino);

        lancamento.setComprovantePath(nomeArquivo);
        return ResponseEntity.ok(service.salvarComprovante(lancamento));
    }

    @GetMapping("/comprovante")
    public ResponseEntity<Resource> baixarComprovante(@RequestParam("arquivo") String arquivo) throws IOException {
        // [LAB] proposital: normalize() só limpa "../" da sintaxe do caminho — não
        // impede que o resultado final fique FORA da pasta "uploads". Sem checar se
        // "caminho" ainda começa com o diretório base, isso é path traversal: dá para
        // ler qualquer arquivo legível pelo processo (ex.: ../src/main/resources/
        // application-secrets.properties).
        Path caminho = Paths.get(UPLOAD_DIR).resolve(arquivo).normalize();
        Resource recurso = new UrlResource(caminho.toUri());

        if (!recurso.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + caminho.getFileName() + "\"")
                .body(recurso);
    }
}
