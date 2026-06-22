import React from 'react';
import { Navigate } from 'react-router-dom';
import { estaAutenticado } from '../../services/authService';

const RotaPrivada = ({ children }) => {
  if (!estaAutenticado()) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default RotaPrivada;
