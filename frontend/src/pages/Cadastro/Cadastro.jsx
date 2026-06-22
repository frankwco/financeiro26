import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Card } from 'primereact/card';
import { InputText } from 'primereact/inputtext';
import { Password } from 'primereact/password';
import { Button } from 'primereact/button';
import { Message } from 'primereact/message';
import UsuarioService from '../../services/UsuarioService';
import './Cadastro.css';

const usuarioService = new UsuarioService();

const Cadastro = () => {
  const navigate = useNavigate();
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmacaoSenha, setConfirmacaoSenha] = useState('');
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');
  const [carregando, setCarregando] = useState(false);

  const realizarCadastro = async (event) => {
    event.preventDefault();
    setErro('');
    setSucesso('');

    if (senha !== confirmacaoSenha) {
      setErro('A confirmacao de senha nao confere.');
      return;
    }

    try {
      setCarregando(true);

      const dados = {
        nome,
        email,
        senha,
      };

      await usuarioService.inserir(dados);
      setSucesso('Cadastro realizado com sucesso.');
      
      setNome("");
      setEmail("");
      setSenha("");
      setConfirmacaoSenha("");
    } catch (erroCadastro) {
      const mensagem =
        erroCadastro?.response?.data?.mensagem ||
        'Nao foi possivel realizar o cadastro.';
      setErro(mensagem);
    } finally {
      setCarregando(false);
    }
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
          {sucesso && <Message severity="success" text={sucesso} />}

          <Button
            type="submit"
            label="Cadastrar"
            className="w-full"
            loading={carregando}
          />

          <div className="links-autenticacao">
            <Link to="/login">Voltar para login</Link>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default Cadastro;
