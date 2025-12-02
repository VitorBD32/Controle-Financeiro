package br.uespi.pessoas;

import java.util.ArrayList;

public class listaDePessoas {

	ArrayList<pessoa> listaDeNomes;

	public listaDePessoas() {
		listaDeNomes = new ArrayList<>();
	}

	public void adicionarPessoa(pessoa p1) {
		listaDeNomes.add(p1);
	}
        
	public void adicionarPessoa(int pos, pessoa p1) {
		listaDeNomes.add(pos, p1);
	}

	public void modificaNo(pessoa p1, int ind) {
		listaDeNomes.set(ind,p1);
	}

	public String[] retornaLista() {
		String[] nomes = new String[listaDeNomes.size()];
		int cont = 0;
		for (pessoa pp : listaDeNomes) {
			nomes[cont] = pp.getNome() + " || " + pp.getFone() + " || " + pp.getCpf();
			cont++;
        	};
		return nomes;
	}

	public void imprimeLista(int id) {
		System.out.print("Imprimindo Elemento da Posicao [");
		System.out.print(id);
		System.out.print("]: ");
            	System.out.println(listaDeNomes.get(id).getNome());
		System.out.println("");
	}

	public String[] buscaNome(String busca) {
		String[] nomes = new String[listaDeNomes.size()];
		int cont = 0;
		for (pessoa pp : listaDeNomes) {
			if (busca.equals(pp.getNome())) {
				nomes[cont] = pp.getNome() + " || " + pp.getFone() + " || " + pp.getCpf();
			}
			cont++;
        	};
		return nomes;
	}

}