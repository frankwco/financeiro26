package com.ifpr.backend.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * [SSE] Guarda as conexões abertas (uma SseEmitter por aba/navegador conectado)
 * agrupadas pelo id do usuário, e sabe enviar um evento para todas as conexões
 * de um usuário.
 */
@Service
public class LancamentoEventosService {

    // 30 minutos. Passou disso, o navegador (EventSource) reconecta sozinho.
    private static final long TIMEOUT_MS = 30 * 60 * 1000L;

    // idUsuario -> conexões abertas dele (ex.: 2 abas = 2 emitters)
    private final Map<Long, List<SseEmitter>> conexoes = new ConcurrentHashMap<>();

    public SseEmitter registrar(Long idUsuario) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        List<SseEmitter> lista = conexoes.computeIfAbsent(idUsuario, id -> new CopyOnWriteArrayList<>());
        lista.add(emitter);

        // Limpeza: quando a aba fecha, estoura timeout ou dá erro, tira da lista.
        emitter.onCompletion(() -> lista.remove(emitter));
        emitter.onTimeout(() -> lista.remove(emitter));
        emitter.onError(e -> lista.remove(emitter));

        // Evento inicial: confirma para o frontend que a conexão está aberta.
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
