import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Card } from 'primereact/card';
import { InputText } from 'primereact/inputtext';
import { Password } from 'primereact/password';
import { Button } from 'primereact/button';
import { Message } from 'primereact/message';
import './Cadastro.css';

const Cadastro = () => {
  const navigate = useNavigate();
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmacaoSenha, setConfirmacaoSenha] = useState('');
  const [erro, setErro] = useState('');

  const realizarCadastro = (event) => {
    event.preventDefault();
    setErro('');

    if (senha !== confirmacaoSenha) {
      setErro('A confirmacao de senha nao confere.');
      return;
    }

    navigate('/login');
  };

  return (
    <div className="pagina-autenticacao">
      <Card title="Novo Cadastro" className="cartao-autenticacao">
        <form onSubmit={realizarCadastro} className="formulario-autenticacao">
          <span className="p-float-label">
            <InputText
              id="register-name"
              value={nome}
              onChange={(event) => setNome(event.target.value)}
              className="w-full"
            />
            <label htmlFor="register-name">Nome</label>
          </span>

          <span className="p-float-label">
            <InputText
              id="register-email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              className="w-full"
            />
            <label htmlFor="register-email">Email</label>
          </span>

          <span className="p-float-label">
            <Password
              id="register-password"
              value={senha}
              onChange={(event) => setSenha(event.target.value)}
              feedback={false}
              toggleMask
              className="w-full"
              inputClassName="w-full"
            />
            <label htmlFor="register-password">Senha</label>
          </span>

          <span className="p-float-label">
            <Password
              id="register-confirm-password"
              value={confirmacaoSenha}
              onChange={(event) => setConfirmacaoSenha(event.target.value)}
              feedback={false}
              toggleMask
              className="w-full"
              inputClassName="w-full"
            />
            <label htmlFor="register-confirm-password">Confirmar senha</label>
          </span>

          {erro && <Message severity="error" text={erro} />}

          <Button type="submit" label="Cadastrar" className="w-full" />

          <div className="links-autenticacao">
            <Link to="/login">Voltar para login</Link>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default Cadastro;
