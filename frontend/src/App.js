import React from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import RotaPrivada from './components/PrivateRoute/PrivateRoute';
import Dashboard from './pages/Dashboard/Dashboard';
import RecuperacaoSenha from './pages/RecuperacaoSenha/RecuperacaoSenha';
import Login from './pages/Login/Login';
import CadastroUsuario from './pages/CadastroUsuario/CadastroUsuario';
import Lancamentos from './pages/Lancamentos/Lancamentos';

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/recuperar-senha" element={<RecuperacaoSenha />} />
      <Route path="/novo-cadastro" element={<CadastroUsuario />} />
      <Route
        path="/dashboard"
        element={
          <RotaPrivada>
            <Dashboard />
          </RotaPrivada>
        }
      />
      <Route
        path="/lancamentos"
        element={
          <RotaPrivada>
            <Lancamentos />
          </RotaPrivada>
        }
      />
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default App;
