import BaseService from './BaseService';

class LancamentoService extends BaseService {
  constructor() {
    super('/lancamento');
  }
}

export default LancamentoService;
