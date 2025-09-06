package controle.model;

import java.math.BigDecimal;

public class Categoria {

    private int id;
    private String nome;
    private BigDecimal valor;
    private String descricao;

    public Categoria() {
    }

    public Categoria(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Categoria(int id, String nome, BigDecimal valor, String descricao) {
        this.id = id;
        this.nome = nome;
        this.valor = valor;
        this.descricao = descricao;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return nome != null ? nome : "Categoria{id=" + id + "}";
    }
}
