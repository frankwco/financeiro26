import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Card } from 'primereact/card';
import { InputText } from 'primereact/inputtext';
import { Password } from 'primereact/password';
import { Button } from 'primereact/button';
import { Message } from 'primereact/message';
import { fazerLogin } from '../../services/authService';
import './Login.css';

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState('');

  const realizarLogin = (event) => {
    event.preventDefault();
    setErro('');

    try {
      fazerLogin({ email, senha });
      navigate('/dashboard');
    } catch (erroLogin) {
      setErro(erroLogin.message);
    }
  };

  return (
    <div className="pagina-autenticacao">
      <Card title="Login" className="cartao-autenticacao">
        <form onSubmit={realizarLogin} className="formulario-autenticacao">
          <span className="p-float-label">
            <InputText
              id="login-email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              className="w-full"
            />
            <label htmlFor="login-email">Email</label>
          </span>

          <span className="p-float-label">
            <Password
              id="login-password"
              value={senha}
              onChange={(event) => setSenha(event.target.value)}
              feedback={false}
              toggleMask
              className="w-full"
              inputClassName="w-full"
            />
            <label htmlFor="login-password">Senha</label>
          </span>

          {erro && <Message severity="error" text={erro} />}

          <Button type="submit" label="Entrar" className="w-full" />

          <div className="links-autenticacao">
            <Link to="/recuperar-senha">Esqueci minha senha</Link>
            <Link to="/novo-cadastro">Novo cadastro</Link>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default Login;
