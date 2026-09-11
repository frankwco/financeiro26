import React from 'react';
import { Menubar } from 'primereact/menubar';
import { useNavigate } from 'react-router-dom';
import { sairDaConta } from '../../services/AutenticacaoService';

const AppMenu = () => {
  const navigate = useNavigate();

  const itensMenu = [
    {
      label: 'Dashboard',
      icon: 'pi pi-home',
      command: () => navigate('/dashboard'),
    },
    {
      label: 'Lançamentos',
      icon: 'pi pi-wallet',
      command: () => navigate('/lancamentos'),
    },
    {
      label: 'Todos os Lançamentos (Admin)',
      icon: 'pi pi-shield',
      command: () => navigate('/admin/lancamentos'),
    },
    {
      label: 'Sair',
      icon: 'pi pi-sign-out',
      command: async () => {
        await sairDaConta();
        navigate('/login');
      },
    },
  ];

  return <Menubar model={itensMenu} />;
};

export default AppMenu;
