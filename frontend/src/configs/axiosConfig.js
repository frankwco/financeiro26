import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8081',
  withCredentials: true, // envia/recebe o cookie httpOnly do refresh token
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
    if (usuario?.token) {
      config.headers.Authorization = `Bearer ${usuario.token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

let renovacaoEmAndamento = null;

const renovarAccessToken = () => {
  if (!renovacaoEmAndamento) {
    renovacaoEmAndamento = api
      .post('/autenticacao/refresh')
      .then((resposta) => {
        const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
        const usuarioAtualizado = { ...usuario, ...resposta.data };
        localStorage.setItem('usuario', JSON.stringify(usuarioAtualizado));
        return usuarioAtualizado.token;
      })
      .catch((erro) => {
        localStorage.removeItem('usuario');
        window.location.href = '/login';
        throw erro;
      })
      .finally(() => {
        renovacaoEmAndamento = null;
      });
  }
  return renovacaoEmAndamento;
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const requisicaoOriginal = error.config;
    const isRefreshCall = requisicaoOriginal?.url?.includes('/autenticacao/refresh');

    if (error.response?.status === 401 && !requisicaoOriginal._retry && !isRefreshCall) {
      requisicaoOriginal._retry = true;
      const novoToken = await renovarAccessToken();
      requisicaoOriginal.headers.Authorization = `Bearer ${novoToken}`;
      return api(requisicaoOriginal);
    }

    return Promise.reject(error);
  }
);

export default api;
