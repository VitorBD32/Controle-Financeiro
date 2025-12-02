package br.uespi.acessoapi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Classe para conexão com API de pagamentos PIX
 * Utiliza o tratamento JSON do pacote br.uespi.tratajson
 */
public class PIXConexao {

    private String url;
    private String urlParameters;
    private int codRetorno;
    private String resposta;

    // Dados do pagamento PIX
    private String chavePix;
    private String valor;
    private String descricao;
    private String nomeRecebedor;

    public PIXConexao(String url) {
        this.url = url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Configura os dados para um pagamento PIX
     */
    public void setDadosPix(String chavePix, String valor, String descricao, String nomeRecebedor) throws Exception {
        this.chavePix = chavePix;
        this.valor = valor;
        this.descricao = descricao;
        this.nomeRecebedor = nomeRecebedor;
        
        this.urlParameters = "chave_pix=" + URLEncoder.encode(chavePix, "UTF-8")
                + "&valor=" + URLEncoder.encode(valor, "UTF-8")
                + "&descricao=" + URLEncoder.encode(descricao, "UTF-8")
                + "&nome_recebedor=" + URLEncoder.encode(nomeRecebedor, "UTF-8")
                + "&tipopag=" + URLEncoder.encode("PIX", "UTF-8");
    }

    /**
     * Configura os parâmetros para consulta de status PIX
     */
    public void setConsultaPix(String transacaoId) throws Exception {
        this.urlParameters = "transacao_id=" + URLEncoder.encode(transacaoId, "UTF-8")
                + "&acao=" + URLEncoder.encode("consulta", "UTF-8");
    }

    /**
     * Configura parâmetros de autenticação para a API
     */
    public void setAutenticacao(String usuario, String senha) throws Exception {
        if (this.urlParameters == null || this.urlParameters.isEmpty()) {
            this.urlParameters = "";
        } else {
            this.urlParameters += "&";
        }
        this.urlParameters += "usuario=" + URLEncoder.encode(usuario, "UTF-8")
                + "&senha=" + URLEncoder.encode(senha, "UTF-8");
    }

    /**
     * Realiza a conexão com a API e retorna a resposta JSON
     */
    public String conectar() throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Configura método POST
        con.setRequestMethod("POST");

        // Headers
        con.setRequestProperty("User-Agent", "ControleFinanceiro-Desktop/1.0");
        con.setRequestProperty("Accept-Language", "pt-BR,pt;q=0.9");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("Accept", "application/json");

        // Timeout de conexão
        con.setConnectTimeout(10000);
        con.setReadTimeout(10000);

        // Envia requisição POST
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters != null ? urlParameters : "");
        wr.flush();
        wr.close();

        // Obtém código de retorno
        this.codRetorno = con.getResponseCode();

        // Lê a resposta
        BufferedReader in;
        if (codRetorno >= 200 && codRetorno < 300) {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }

        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        this.resposta = response.toString();
        return this.resposta;
    }

    /**
     * Realiza conexão GET para consultas
     */
    public String consultarGet(String endpoint) throws Exception {
        URL obj = new URL(url + endpoint);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "ControleFinanceiro-Desktop/1.0");
        con.setRequestProperty("Accept", "application/json");
        con.setConnectTimeout(10000);
        con.setReadTimeout(10000);

        this.codRetorno = con.getResponseCode();

        BufferedReader in;
        if (codRetorno >= 200 && codRetorno < 300) {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }

        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        this.resposta = response.toString();
        return this.resposta;
    }

    public int getCodRetorno() {
        return codRetorno;
    }

    public String getResposta() {
        return resposta;
    }

    public String getChavePix() {
        return chavePix;
    }

    public String getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getNomeRecebedor() {
        return nomeRecebedor;
    }
}
