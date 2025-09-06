package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transacao {

    private int id;
    private int idUsuario;
    private int idCategoria;
    private String tipo; // "Receita" ou "Despesa"
    private BigDecimal valor;
    private LocalDate data;
    private String descricao;

    public Transacao() {
    }

    // getters/setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return "Transacao{id=" + id + ", usuario=" + idUsuario + ", categoria=" + idCategoria + ", tipo='" + tipo + "', valor=" + valor + ", data=" + data + "}";
    }
}
