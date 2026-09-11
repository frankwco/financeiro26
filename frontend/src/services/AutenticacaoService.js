import api from '../configs/axiosConfig';

const CHAVE_USUARIO = 'usuario';

export const fazerLogin = async ({ email, senha }) => {
  try {
    const resposta = await api.post('/autenticacao/login', { email, senha });
    localStorage.setItem(CHAVE_USUARIO, JSON.stringify(resposta.data));
    return resposta.data;
  } catch (erro) {
    throw new Error('Email ou senha invalidos.');
  }
};

export const solicitarRecuperacaoSenha = async (email) => {
  try {
    const resposta = await api.post('/autenticacao/recuperar-senha', { email });
    return resposta.data;
  } catch (erro) {
    throw new Error('Nao foi possivel solicitar a recuperacao de senha.');
  }
};

export const redefinirSenha = async ({ email, token, novaSenha }) => {
  try {
    await api.post('/autenticacao/redefinir-senha', { email, token, novaSenha });
  } catch (erro) {
    throw new Error('Codigo invalido ou expirado.');
  }
};

export const sairDaConta = async () => {
  try {
    await api.post('/autenticacao/logout');
  } catch (erro) {
    // mesmo se a chamada falhar, a sessao local eh encerrada abaixo
  } finally {
    localStorage.removeItem(CHAVE_USUARIO);
  }
};

export const estaAutenticado = () => Boolean(localStorage.getItem(CHAVE_USUARIO));

export const obterUsuarioAtual = () => JSON.parse(localStorage.getItem(CHAVE_USUARIO) || 'null');
