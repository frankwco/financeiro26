import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Card } from 'primereact/card';
import { InputText } from 'primereact/inputtext';
import { Password } from 'primereact/password';
import { Button } from 'primereact/button';
import { Message } from 'primereact/message';
import { solicitarRecuperacaoSenha, redefinirSenha } from '../../services/AutenticacaoService';
import './RecuperacaoSenha.css';

const RecuperacaoSenha = () => {
  const navigate = useNavigate();
  const [etapa, setEtapa] = useState('email');
  const [email, setEmail] = useState('');
  const [token, setToken] = useState('');
  const [novaSenha, setNovaSenha] = useState('');
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');
  const [carregando, setCarregando] = useState(false);

  const solicitar = async (event) => {
    event.preventDefault();
    setErro('');
    setSucesso('');
    setCarregando(true);
    try {
      await solicitarRecuperacaoSenha(email);
      setSucesso('Se o e-mail existir, enviamos um código de recuperação.');
      setEtapa('redefinir');
    } catch (erroSolicitar) {
      setErro(erroSolicitar.message);
    } finally {
      setCarregando(false);
    }
  };

  const redefinir = async (event) => {
    event.preventDefault();
    setErro('');
    setSucesso('');
    setCarregando(true);
    try {
      await redefinirSenha({ email, token, novaSenha });
      setSucesso('Senha redefinida com sucesso. Você já pode fazer login.');
      setTimeout(() => navigate('/login'), 1500);
    } catch (erroRedefinir) {
      setErro(erroRedefinir.message);
    } finally {
      setCarregando(false);
    }
  };

  return (
    <div className="pagina-autenticacao">
      <Card title="Recuperação de Senha" className="cartao-autenticacao">
        {etapa === 'email' && (
          <form onSubmit={solicitar} className="formulario-autenticacao">
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

            <Button type="submit" label="Enviar código" className="w-full" loading={carregando} />

            <div className="links-autenticacao">
              <Link to="/login">Voltar para login</Link>
            </div>
          </form>
        )}

        {etapa === 'redefinir' && (
          <form onSubmit={redefinir} className="formulario-autenticacao">
            <span className="p-float-label">
              <InputText
                id="forgot-token"
                value={token}
                onChange={(event) => setToken(event.target.value)}
                className="w-full"
              />
              <label htmlFor="forgot-token">Código recebido por e-mail</label>
            </span>

            <span className="p-float-label">
              <Password
                id="forgot-nova-senha"
                value={novaSenha}
                onChange={(event) => setNovaSenha(event.target.value)}
                feedback={false}
                toggleMask
                className="w-full"
                inputClassName="w-full"
              />
              <label htmlFor="forgot-nova-senha">Nova senha</label>
            </span>

            {erro && <Message severity="error" text={erro} />}
            {sucesso && <Message severity="success" text={sucesso} />}

            <Button type="submit" label="Redefinir senha" className="w-full" loading={carregando} />

            <div className="links-autenticacao">
              <Link to="/login">Voltar para login</Link>
            </div>
          </form>
        )}
      </Card>
    </div>
  );
};

export default RecuperacaoSenha;
