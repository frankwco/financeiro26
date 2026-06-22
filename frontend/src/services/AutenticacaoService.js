
//PROVISÓRIO

const CHAVE_TOKEN = 'app-token';
const EMAIL_FIXO = 'frankwco@gmail.com';
const SENHA_FIXA = '123';
const USUARIO_FIXO = {
  nome: 'Frank',
  email: EMAIL_FIXO,
};


export const fazerLogin = ({ email, senha }) => {
  const emailNormalizado = email.trim().toLowerCase();
  const senhaInformada = String(senha).trim();

  if (emailNormalizado !== EMAIL_FIXO || senhaInformada !== SENHA_FIXA) {
    throw new Error('Email ou senha invalidos.');
  }

  localStorage.setItem(CHAVE_TOKEN, 'token-fixo');
  return 'token-fixo';
};

export const recuperarSenha = (email) => {
  const emailNormalizado = email.trim().toLowerCase();

  if (emailNormalizado !== EMAIL_FIXO) {
    throw new Error('Use o email frankwco@gmail.com.');
  }

  return 'Sua senha e 123.';
};

export const sairDaConta = () => {
  localStorage.removeItem(CHAVE_TOKEN);
};

export const estaAutenticado = () => Boolean(localStorage.getItem(CHAVE_TOKEN));

export const obterUsuarioAtual = () => USUARIO_FIXO;
