import BaseService from './BaseService';

class LancamentoService extends BaseService {
  constructor() {
    super('/lancamento');
  }

  async buscarPorDescricao(termo) {
    const resposta = await this.api.get(`${this.endPoint}/buscar`, { params: { descricao: termo } });
    return resposta;
  }

  async listarTodos() {
    const resposta = await this.api.get(`${this.endPoint}/todos`);
    return resposta;
  }

  async anexarComprovante(id, arquivo) {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    const resposta = await this.api.post(`${this.endPoint}/${id}/comprovante`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return resposta;
  }

  async baixarComprovante(nomeArquivo) {
    const resposta = await this.api.get(`${this.endPoint}/comprovante`, {
      params: { arquivo: nomeArquivo },
      responseType: 'blob',
    });
    return resposta;
  }
}

export default LancamentoService;
