package br.uespi.acessoapi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class pagamentoHTTP {

	public String usuario, senha, url, urlParameters, resposta;
	public int codretorno;

	public  void setUrl(String ur) {
		this.url = ur;
	}

	public void setDados(String nome, String cpf, String ncartao, String valor, String tipopag) throws Exception {
		this.urlParameters = "nome=" + URLEncoder.encode(nome, "UTF-8") 
				   + "&cpf=" + URLEncoder.encode(cpf, "UTF-8")
				   + "&ncartao=" + URLEncoder.encode(ncartao, "UTF-8")
				   + "&valor=" + URLEncoder.encode(valor, "UTF-8")
				   + "&tipopag=" + URLEncoder.encode(tipopag, "UTF-8");
	}

	// Método para adicionar autenticação aos parâmetros
	public void setAutenticacao(String usuario, String senha) throws Exception {
		this.usuario = usuario;
		this.senha = senha;
		this.urlParameters += "&usuario=" + URLEncoder.encode(usuario, "UTF-8")
				   + "&senha=" + URLEncoder.encode(senha, "UTF-8");
	}

	public pagamentoHTTP(String nome, String cpf, String ncartao, String valor, String tipopag, String ur) throws Exception {
		this.setUrl(ur);
		this.setDados(nome,cpf,ncartao,valor,tipopag);
	}

	// Construtor com autenticação
	public pagamentoHTTP(String nome, String cpf, String ncartao, String valor, String tipopag, String ur, String usuario, String senha) throws Exception {
		this.setUrl(ur);
		this.setDados(nome,cpf,ncartao,valor,tipopag);
		this.setAutenticacao(usuario, senha);
	}

        public String conecta() throws Exception {
        	URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// Set request method - HTTP POST
		con.setRequestMethod("POST");

		// Set headers - application/x-www-form-urlencoded
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		// Get return Code
		int responseCode = con.getResponseCode();
		this.codretorno = responseCode;

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}
}