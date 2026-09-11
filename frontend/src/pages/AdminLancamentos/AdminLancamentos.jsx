import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import AppMenu from '../../components/AppMenu/AppMenu';
import LancamentoService from '../../services/LancamentoService';
import '../Lancamentos/Lancamentos.css';

const lancamentoService = new LancamentoService();

const AdminLancamentos = () => {
  const [lancamentos, setLancamentos] = useState([]);
  const [erro, setErro] = useState('');

  useEffect(() => {
    lancamentoService
      .listarTodos()
      .then((resposta) => setLancamentos(resposta.data))
      .catch(() => setErro('Acesso negado (este recurso exige o perfil ADMIN) ou o servidor não respondeu.'));
  }, []);

  // [LAB] proposital: renderiza a descricao vinda do banco como HTML cru, sem
  // escapar. Todo o resto do app usa {variavel} (React escapa por padrão) — este
  // é o único lugar que reintroduz uma injeção de verdade, de propósito.
  const colunaDescricao = (linha) => <span dangerouslySetInnerHTML={{ __html: linha.descricao }} />;

  const colunaTipo = (linha) => (
    <Tag severity={linha.tipo === 'RECEITA' ? 'success' : 'danger'} value={linha.tipo} />
  );

  return (
    <div className="pagina-lancamentos">
      <AppMenu />
      <div className="conteudo-lancamentos">
        <Card title="Todos os lançamentos (visão administrativa)">
          {erro && <Message severity="error" text={erro} />}
          <DataTable value={lancamentos} emptyMessage="Nenhum lançamento encontrado.">
            <Column header="Descrição" body={colunaDescricao} />
            <Column field="valor" header="Valor" />
            <Column header="Tipo" body={colunaTipo} />
            <Column field="data" header="Data" />
            <Column header="Usuário" body={(linha) => linha.usuario?.email} />
          </DataTable>
        </Card>
      </div>
    </div>
  );
};

export default AdminLancamentos;
