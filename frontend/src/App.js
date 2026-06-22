import React from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import RotaPrivada from './components/PrivateRoute/PrivateRoute';
import Dashboard from './pages/Dashboard/Dashboard';
import RecuperacaoSenha from './pages/RecuperacaoSenha/RecuperacaoSenha';
import Login from './pages/Login/Login';
import Cadastro from './pages/Cadastro/Cadastro';

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/recuperar-senha" element={<RecuperacaoSenha />} />
      <Route path="/novo-cadastro" element={<Cadastro />} />
      <Route
        path="/dashboard"
        element={
          <RotaPrivada>
            <Dashboard />
          </RotaPrivada>
        }
      />
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default App;
