import React from 'react';
import { Card } from 'primereact/card';
import { Tag } from 'primereact/tag';
import AppMenu from '../../components/AppMenu/AppMenu';
import { obterUsuarioAtual } from '../../services/authService';
import './Dashboard.css';

const Dashboard = () => {
  const usuario = obterUsuarioAtual();

  return (
    <div className="pagina-dashboard">
      <AppMenu />

      <div className="conteudo-dashboard">
        <Card title="Dashboard" subTitle="Area privada">
          <p>
            Usuario autenticado: <strong>{usuario?.nome || 'Usuario'}</strong>
          </p>
          <p>Email: {usuario?.email || '-'}</p>
          <Tag severity="success" value="Token ativo" />
        </Card>
      </div>
    </div>
  );
};

export default Dashboard;
