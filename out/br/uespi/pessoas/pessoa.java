package br.uespi.pessoas;

public class pessoa {
	
	String nome;
	String fone;
	String cpf;
	String id;
	String modo;
	String retmsg;
	String valor;

	public pessoa(String n, String f, String c, String i, String m, String r, String v) {
		this.nome = n;
		this.fone = f;
		this.cpf = c;
		this.id = i;
		this.modo = m;
		this.retmsg = r;
		this.valor = v;
	}

	public void setPessoa(String n, String f, String c, String i, String m, String r, String v) {
		this.nome = n;
		this.fone = f;
		this.cpf = c;
		this.id = i;
		this.modo = m;
		this.retmsg = r;
		this.valor = v;
	}

	public String getNome() {
		return this.nome;
	}

	public String getFone() {
		return this.fone;
	}

	public String getCpf() {
		return this.cpf;
	}

	public String getId() {
		return this.id;
	}

	public String getModo() {
		return this.modo;
	}

	public String getRetmsg() {
		return this.retmsg;
	}

	public String getValor() {
		return this.valor;
	}

}