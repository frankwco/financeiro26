package com.ifpr.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ifpr.backend.model.Lancamento;
import com.ifpr.backend.model.Usuario;
import com.ifpr.backend.repository.LancamentoRepository;
import com.ifpr.backend.repository.UsuarioRepository;
import com.ifpr.backend.security.AuthUsuarioProvider;

@Service
public class LancamentoService {

    @Autowired
    private LancamentoRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthUsuarioProvider authUsuarioProvider;

    public Lancamento inserir(Lancamento lancamento) {
        // [LAB] proposital: se o cliente mandar um "usuario" no corpo da requisição,
        // ele é aceito como o dono do lançamento — deveria SEMPRE usar o autenticado,
        // ignorando qualquer "usuario" vindo de fora.
        if (lancamento.getUsuario() != null && lancamento.getUsuario().getId() != null) {
            Usuario usuarioAlvo = usuarioRepository.findById(lancamento.getUsuario().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            lancamento.setUsuario(usuarioAlvo);
        } else {
            lancamento.setUsuario(authUsuarioProvider.getUsuarioAutenticado());
        }

        // [LAB] proposital: nenhuma verificação de negócio aqui (valor > 0, tipo
        // condizente com o sinal, limite de saldo). Tudo isso só existe no frontend.
        return repository.save(lancamento);
    }

    public List<Lancamento> listarMeusLancamentos() {
        Usuario usuarioLogado = authUsuarioProvider.getUsuarioAutenticado();
        return repository.findByUsuario(usuarioLogado);
    }

    public Lancamento buscarPorId(Long id) {
        // [LAB] proposital: qualquer usuário autenticado pode buscar QUALQUER
        // lançamento por id — não há checagem de que o id pertence a quem pediu.
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lançamento não encontrado"));
    }

    public Lancamento alterar(Lancamento lancamento) {
        // [LAB] proposital: mesma falta de checagem de dono do buscarPorId,
        // então também é possível alterar lançamentos de outros usuários.
        Lancamento lancamentoDB = buscarPorId(lancamento.getId());
        lancamentoDB.setDescricao(lancamento.getDescricao());
        lancamentoDB.setValor(lancamento.getValor());
        lancamentoDB.setTipo(lancamento.getTipo());
        lancamentoDB.setData(lancamento.getData());
        return repository.save(lancamentoDB);
    }

    public void remover(Long id) {
        // [LAB] proposital: idem — remove qualquer id, de qualquer dono.
        Lancamento lancamento = buscarPorId(id);
        repository.delete(lancamento);
    }
}
