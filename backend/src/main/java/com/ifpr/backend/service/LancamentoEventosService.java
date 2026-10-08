package com.ifpr.backend.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Service
public class LancamentoEventosService {

    private static final long TIMEOUT_MS = 30 * 60 * 1000L;

    private final Map<Long, List<SseEmitter>> conexoes = new ConcurrentHashMap<>();

    public SseEmitter registrar(Long idUsuario) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        List<SseEmitter> lista = conexoes.computeIfAbsent(idUsuario, id -> new CopyOnWriteArrayList<>());
        lista.add(emitter);

        emitter.onCompletion(() -> lista.remove(emitter));
        emitter.onTimeout(() -> lista.remove(emitter));
        emitter.onError(e -> lista.remove(emitter));

        enviar(emitter, "conectado", "ok");
        return emitter;
    }

    public void notificar(Long idUsuario, String acao) {
        List<SseEmitter> lista = conexoes.get(idUsuario);
        if (lista == null) {
            return;
        }
        for (SseEmitter emitter : lista) {
            if (!enviar(emitter, "lancamento-alterado", acao)) {
                lista.remove(emitter);
            }
        }
    }

    private boolean enviar(SseEmitter emitter, String nomeEvento, String dados) {
        try {
            emitter.send(SseEmitter.event().name(nomeEvento).data(dados));
            return true;
        } catch (IOException | IllegalStateException e) {
            // Cliente já foi embora: descarta a conexão.
            return false;
        }
    }
}
