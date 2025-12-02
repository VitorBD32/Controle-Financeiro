package br.uespi.tratajson;

import br.uespi.pessoas.*;

public class trataJSON {

	String stringJSON, id, valor_autorizado, modo, cod_retorno, msg;
	pessoa p1;

	public trataJSON(String entrada) {
		this.stringJSON = entrada;
	}

	public pessoa tratarString() {

		JSONObject my_obj = new JSONObject(this.stringJSON);

		this.id = my_obj.getString("id");
		this.valor_autorizado = my_obj.getString("valor_autorizado");
		this.modo = my_obj.getString("modo");
		this.cod_retorno = my_obj.getString("cod_retorno");
		this.msg = my_obj.getString("msg");

		p1 = new pessoa("","","",this.id,this.modo,this.msg,this.valor_autorizado);

		return p1;

	}
}