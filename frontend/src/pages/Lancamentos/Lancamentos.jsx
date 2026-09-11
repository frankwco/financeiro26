import React, { useEffect, useRef, useState } from 'react';
import { Card } from 'primereact/card';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Dropdown } from 'primereact/dropdown';
import { Calendar } from 'primereact/calendar';
import { Button } from 'primereact/button';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import AppMenu from '../../components/AppMenu/AppMenu';
import LancamentoService from '../../services/LancamentoService';
import './Lancamentos.css';

const lancamentoService = new LancamentoService();

const TIPOS = [
  { label: 'Receita', value: 'RECEITA' },
  { label: 'Despesa', value: 'DESPESA' },
];

const vazio = { id: null, descricao: '', valor: null, tipo: 'DESPESA', data: new Date() };

const Lancamentos = () => {
  const [lancamentos, setLancamentos] = useState([]);
  const [form, setForm] = useState(vazio);
  const [erro, setErro] = useState('');
  const [carregando, setCarregando] = useState(false);
  const [termoBusca, setTermoBusca] = useState('');
  const [buscaAtiva, setBuscaAtiva] = useState(false);
  const fileInputRef = useRef(null);
  const [lancamentoParaAnexar, setLancamentoParaAnexar] = useState(null);

  const carregar = async () => {
    try {
      const resposta = await lancamentoService.buscarTodos();
      setLancamentos(resposta.data);
      setBuscaAtiva(false);
    } catch (erroCarregar) {
      setErro('Não foi possível carregar os lançamentos.');
    }
  };

  useEffect(() => {
    carregar();
  }, []);

  const buscar = async (event) => {
    event.preventDefault();
    setErro('');
    try {
      const resposta = await lancamentoService.buscarPorDescricao(termoBusca);
      setLancamentos(resposta.data);
      setBuscaAtiva(true);
    } catch (erroBuscar) {
      setErro('Não foi possível buscar os lançamentos.');
    }
  };

  const limparBusca = () => {
    setTermoBusca('');
    carregar();
  };

  const saldoDisponivel = (ignorarId = null) =>
    lancamentos
      .filter((l) => l.id !== ignorarId)
      .reduce((acc, l) => acc + (l.tipo === 'RECEITA' ? Number(l.valor) : -Number(l.valor)), 0);

  const saldoAtual = saldoDisponivel();

  const limparFormulario = () => setForm(vazio);

  const validarNoFrontend = () => {
    // Regra de negócio validada só aqui, no cliente — o backend aceita
    // qualquer valor/tipo, sem checar nada disso (ver LancamentoService.java).
    if (!form.valor || Number(form.valor) <= 0) {
      return 'O valor precisa ser maior que zero.';
    }
    if (form.tipo === 'DESPESA' && Number(form.valor) > saldoDisponivel(form.id)) {
      return `Saldo insuficiente. Disponível: R$ ${saldoDisponivel(form.id).toFixed(2)}.`;
    }
    return '';
  };

  const salvar = async (event) => {
    event.preventDefault();
    setErro('');

    const mensagemValidacao = validarNoFrontend();
    if (mensagemValidacao) {
      setErro(mensagemValidacao);
      return;
    }

    const dados = {
      id: form.id,
      descricao: form.descricao,
      valor: Number(form.valor),
      tipo: form.tipo,
      data: form.data instanceof Date ? form.data.toISOString().split('T')[0] : form.data,
    };

    setCarregando(true);
    try {
      if (form.id) {
        await lancamentoService.alterar(dados);
      } else {
        await lancamentoService.inserir(dados);
      }
      limparFormulario();
      await carregar();
    } catch (erroSalvar) {
      setErro(
        erroSalvar?.response?.data?.mensagem || 'Não foi possível salvar o lançamento.'
      );
    } finally {
      setCarregando(false);
    }
  };

  const editar = (lancamento) => {
    setForm({
      ...lancamento,
      data: lancamento.data ? new Date(lancamento.data) : new Date(),
    });
  };

  const excluir = async (lancamento) => {
    try {
      await lancamentoService.excluir(lancamento.id);
      await carregar();
    } catch (erroExcluir) {
      setErro('Não foi possível excluir o lançamento.');
    }
  };

  const abrirSeletorArquivo = (lancamento) => {
    setLancamentoParaAnexar(lancamento.id);
    fileInputRef.current.click();
  };

  const aoSelecionarArquivo = async (event) => {
    const arquivo = event.target.files[0];
    event.target.value = '';
    if (!arquivo || !lancamentoParaAnexar) {
      return;
    }
    try {
      await lancamentoService.anexarComprovante(lancamentoParaAnexar, arquivo);
      await carregar();
    } catch (erroAnexar) {
      setErro('Não foi possível anexar o comprovante.');
    }
  };

  const baixarComprovante = async (linha) => {
    if (!linha.comprovantePath) {
      return;
    }
    try {
      const resposta = await lancamentoService.baixarComprovante(linha.comprovantePath);
      const url = window.URL.createObjectURL(resposta.data);
      const link = document.createElement('a');
      link.href = url;
      link.download = linha.comprovantePath;
      link.click();
      window.URL.revokeObjectURL(url);
    } catch (erroBaixar) {
      setErro('Não foi possível baixar o comprovante.');
    }
  };

  const colunaValor = (linha) => (
    <span style={{ color: linha.tipo === 'RECEITA' ? '#2e7d32' : '#c62828' }}>
      {linha.tipo === 'DESPESA' ? '- ' : ''}R$ {Number(linha.valor).toFixed(2)}
    </span>
  );

  const colunaTipo = (linha) => (
    <Tag
      severity={linha.tipo === 'RECEITA' ? 'success' : 'danger'}
      value={linha.tipo === 'RECEITA' ? 'Receita' : 'Despesa'}
    />
  );

  const colunaAcoes = (linha) => (
    <div className="acoes-tabela">
      <Button icon="pi pi-pencil" rounded text onClick={() => editar(linha)} />
      <Button icon="pi pi-trash" rounded text severity="danger" onClick={() => excluir(linha)} />
      <Button
        icon="pi pi-paperclip"
        rounded
        text
        severity={linha.comprovantePath ? 'success' : undefined}
        onClick={() => abrirSeletorArquivo(linha)}
      />
      {linha.comprovantePath && (
        <Button icon="pi pi-download" rounded text onClick={() => baixarComprovante(linha)} />
      )}
    </div>
  );

  return (
    <div className="pagina-lancamentos">
      <AppMenu />
      <input type="file" ref={fileInputRef} style={{ display: 'none' }} onChange={aoSelecionarArquivo} />
      <div className="conteudo-lancamentos">
        <Card>
          <div className="resumo-saldo">
            <span>Saldo atual:</span>
            <Tag
              severity={saldoAtual >= 0 ? 'success' : 'danger'}
              value={`R$ ${saldoAtual.toFixed(2)}`}
            />
          </div>
        </Card>

        <Card title={form.id ? 'Editar lançamento' : 'Novo lançamento'}>
          <form onSubmit={salvar} className="formulario-lancamento">
            <div className="campo">
              <label htmlFor="descricao">Descrição</label>
              <InputText
                id="descricao"
                value={form.descricao}
                onChange={(e) => setForm({ ...form, descricao: e.target.value })}
              />
            </div>

            <div className="campo">
              <label htmlFor="valor">Valor</label>
              <InputNumber
                id="valor"
                value={form.valor}
                onValueChange={(e) => setForm({ ...form, valor: e.value })}
                mode="currency"
                currency="BRL"
                locale="pt-BR"
              />
            </div>

            <div className="campo">
              <label htmlFor="tipo">Tipo</label>
              <Dropdown
                id="tipo"
                value={form.tipo}
                options={TIPOS}
                onChange={(e) => setForm({ ...form, tipo: e.value })}
              />
            </div>

            <div className="campo">
              <label htmlFor="data">Data</label>
              <Calendar
                id="data"
                value={form.data}
                onChange={(e) => setForm({ ...form, data: e.value })}
                dateFormat="dd/mm/yy"
              />
            </div>

            <div className="acoes-formulario">
              <Button type="submit" label={form.id ? 'Salvar' : 'Adicionar'} loading={carregando} />
              {form.id && (
                <Button type="button" label="Cancelar" severity="secondary" outlined onClick={limparFormulario} />
              )}
            </div>
          </form>

          {erro && <Message severity="error" text={erro} className="w-full" style={{ marginTop: 16 }} />}
        </Card>

        <Card title="Meus lançamentos">
          <form onSubmit={buscar} className="formulario-busca">
            <InputText
              placeholder="Buscar por descrição"
              value={termoBusca}
              onChange={(e) => setTermoBusca(e.target.value)}
            />
            <Button type="submit" label="Buscar" outlined />
            {buscaAtiva && (
              <Button type="button" label="Limpar busca" severity="secondary" text onClick={limparBusca} />
            )}
          </form>

          <DataTable value={lancamentos} emptyMessage="Nenhum lançamento cadastrado.">
            <Column field="descricao" header="Descrição" />
            <Column header="Valor" body={colunaValor} />
            <Column header="Tipo" body={colunaTipo} />
            <Column field="data" header="Data" />
            <Column header="Ações" body={colunaAcoes} />
          </DataTable>
        </Card>
      </div>
    </div>
  );
};

export default Lancamentos;
