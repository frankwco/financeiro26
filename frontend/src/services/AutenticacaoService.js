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

export const recuperarSenha = (email) => {
  throw new Error('Recuperacao de senha ainda nao disponivel.');
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
