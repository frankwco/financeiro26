package com.ifpr.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ifpr.backend.model.Lancamento;
import com.ifpr.backend.model.Usuario;
import com.ifpr.backend.repository.LancamentoRepository;
import com.ifpr.backend.repository.UsuarioRepository;
import com.ifpr.backend.security.AuthUsuarioProvider;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class LancamentoService {

    @Autowired
    private LancamentoRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthUsuarioProvider authUsuarioProvider;

    @Autowired
    private LancamentoEventosService eventos;

    @PersistenceContext
    private EntityManager entityManager;

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
        Lancamento salvo = repository.save(lancamento);
       
        eventos.notificar(salvo.getUsuario().getId(), "inserido"); 
        
        return salvo;
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
        Lancamento salvo = repository.save(lancamentoDB);
        
        eventos.notificar(salvo.getUsuario().getId(), "alterado"); 
        
        return salvo;
    }

    public void remover(Long id) {
        // [LAB] proposital: idem — remove qualquer id, de qualquer dono.
        Lancamento lancamento = buscarPorId(id);
        repository.delete(lancamento);
        eventos.notificar(lancamento.getUsuario().getId(), "removido"); 
    }

    @SuppressWarnings("unchecked")
    public List<Lancamento> buscarPorDescricao(String termo) {
        Usuario usuarioLogado = authUsuarioProvider.getUsuarioAutenticado();

        // [LAB] proposital: consulta nativa montada por concatenação de string em vez
        // de parâmetro (?1/:termo). O "termo" digitado pelo usuário vai direto para o
        // SQL, permitindo injeção — inclusive para escapar do filtro id_usuario abaixo.
        String sql = "SELECT * FROM lancamento WHERE id_usuario = " + usuarioLogado.getId()
                + " AND descricao LIKE '%" + termo + "%'";

        Query query = entityManager.createNativeQuery(sql, Lancamento.class);
        return query.getResultList();
    }

    public List<Lancamento> listarTodos() {
        // Uso administrativo: ver os lançamentos de todos os usuários do sistema.
        // Protegido em LancamentoController com @PreAuthorize("hasAuthority('ADMIN')").
        return repository.findAll();
    }

    public Lancamento salvarComprovante(Lancamento lancamento) {
        return repository.save(lancamento);
    }
}
