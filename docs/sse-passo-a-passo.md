# Server-Sent Events (SSE) no financeiro26 — passo a passo

**Objetivo da demo:** abrir a tela *Lançamentos* em duas abas. Ao adicionar, editar ou
excluir um lançamento em uma aba, a **outra aba atualiza sozinha**, sem F5 e sem polling.

## O que é SSE (30 segundos)
- O navegador faz **uma** requisição `GET` que **não termina**.
- O servidor vai escrevendo eventos nessa resposta (`Content-Type: text/event-stream`).
- Só o servidor → navegador (para o sentido contrário continuam os POST/PUT normais).
- No navegador é a API nativa `EventSource`, que **reconecta sozinha** se cair.
- Formato de cada evento no fio:
  ```
  event: lancamento-alterado
  data: inserido

  ```

## Fluxo
```
Aba A  --POST /lancamento-->  LancamentoService.inserir()
                                   |  eventos.notificar(idUsuario, "inserido")
                                   v
                         LancamentoEventosService  --event: lancamento-alterado-->  Aba B (EventSource)
                                                                                      |
                                                                          carregar()  -> GET /lancamento
```

## Arquivos (4 no backend, 1 no frontend)

### 1. NOVO — `backend/.../service/LancamentoEventosService.java`
Guarda as conexões abertas por usuário e envia eventos. (Código completo no arquivo.)
Pontos para explicar:
- `SseEmitter` = uma conexão aberta (uma por aba).
- `Map<Long, List<SseEmitter>>` = id do usuário → conexões dele.
- `onCompletion/onTimeout/onError` removem a conexão da lista (senão vaza memória).
- `emitter.send(SseEmitter.event().name("...").data("..."))` escreve o evento.

### 2. `LancamentoController.java` — endpoint do stream
```java
@Autowired private LancamentoEventosService eventos;
@Autowired private AuthUsuarioProvider authUsuarioProvider;

@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter stream() {
    return eventos.registrar(authUsuarioProvider.getUsuarioAutenticado().getId());
}
```
Imports novos: `MediaType`, `SseEmitter`, `AuthUsuarioProvider`, `LancamentoEventosService`.

### 3. `LancamentoService.java` — disparar o evento
Injetar `LancamentoEventosService eventos` e chamar `notificar` depois de gravar:
```java
// inserir
Lancamento salvo = repository.save(lancamento);
eventos.notificar(salvo.getUsuario().getId(), "inserido");
return salvo;

// alterar
Lancamento salvo = repository.save(lancamentoDB);
eventos.notificar(salvo.getUsuario().getId(), "alterado");
return salvo;

// remover (depois do repository.delete)
eventos.notificar(lancamento.getUsuario().getId(), "removido");
```

### 4. `JwtFiltroAutenticacao.java` — autenticar o stream
`EventSource` **não permite enviar o header `Authorization`**. Solução simples para a
aula: para esse endpoint, aceitar o token em `?token=`:
```java
if (token == null && request.getRequestURI().endsWith("/lancamento/stream")) {
    token = request.getParameter("token");
    try {
        username = token != null ? jwtService.extractUsername(token) : null;
    } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
        logger.debug("Token JWT (stream) ignorado: " + e.getMessage());
    }
}
```
(Colocar logo antes do `if (username != null && ...)` que já existia.)

### 5. `frontend/src/pages/Lancamentos/Lancamentos.jsx`
Estado: `const [aoVivo, setAoVivo] = useState(false);`

Novo `useEffect` (abaixo do que já chama `carregar()`):
```jsx
useEffect(() => {
  const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
  if (!usuario?.token) return undefined;
  const base = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8081';
  const fonte = new EventSource(`${base}/lancamento/stream?token=${usuario.token}`);

  fonte.addEventListener('conectado', () => setAoVivo(true));
  fonte.addEventListener('lancamento-alterado', () => carregar());
  fonte.onerror = () => setAoVivo(false);

  return () => fonte.close();
}, []);
```
Indicador visual, dentro do `<div className="resumo-saldo">`:
```jsx
<Tag severity={aoVivo ? 'info' : 'warning'} icon="pi pi-bolt"
     value={aoVivo ? 'Tempo real: conectado' : 'Tempo real: desconectado'} />
```

## Como demonstrar
1. Subir o backend (**Java 17+**) e o frontend (`npm start`).
2. Logar e abrir *Lançamentos* em **duas abas** (mesmo usuário). Ambas mostram "Tempo real: conectado".
3. Em uma aba, adicionar um lançamento → a outra atualiza sozinha.
4. Mostrar o fio: DevTools → Network → requisição `stream` → aba **EventStream**.
   Ou no terminal: `curl -N "http://localhost:8081/lancamento/stream?token=SEU_JWT"`.
5. Derrubar o backend: o indicador vira "desconectado"; ao subir de novo, reconecta sozinho.

## Limitações (ótimas para discutir em aula)
- Token na URL aparece em logs/histórico — em produção usar cookie ou um token de uso único curto.
- O token do `EventSource` não é renovado: após expirar, a reconexão automática falha.
- `Map` em memória só funciona com **uma instância** do backend (com várias, precisaria de Redis/broker).
- Sem *heartbeat*: proxies podem derrubar conexões ociosas (ideal enviar um comentário `:ping` periódico).
- Se a busca por descrição estiver ativa, o evento recarrega a lista completa e limpa a busca.
