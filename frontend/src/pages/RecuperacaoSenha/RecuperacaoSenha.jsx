import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Card } from 'primereact/card';
import { InputText } from 'primereact/inputtext';
import { Button } from 'primereact/button';
import { Message } from 'primereact/message';
import { recuperarSenha } from '../../services/authService';
import './RecuperacaoSenha.css';

const RecuperacaoSenha = () => {
  const [email, setEmail] = useState('');
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');

  const realizarRecuperacao = (event) => {
    event.preventDefault();
    setErro('');
    setSucesso('');

    try {
      const mensagem = recuperarSenha(email);
      setSucesso(mensagem);
    } catch (erroRecuperacao) {
      setErro(erroRecuperacao.message);
    }
  };

  return (
    <div className="pagina-autenticacao">
      <Card title="Recuperacao de Senha" className="cartao-autenticacao">
        <form onSubmit={realizarRecuperacao} className="formulario-autenticacao">
          <span className="p-float-label">
            <InputText
              id="forgot-email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              className="w-full"
            />
            <label htmlFor="forgot-email">Email</label>
          </span>

          {erro && <Message severity="error" text={erro} />}
          {sucesso && <Message severity="success" text={sucesso} />}

          <Button type="submit" label="Recuperar senha" className="w-full" />

          <div className="links-autenticacao">
            <Link to="/login">Voltar para login</Link>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default RecuperacaoSenha;
