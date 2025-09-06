package controle.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transacao {

    private int id;
    private int idUsuario;
    private int idCategoria;
    private String tipo; // 'D' ou 'C'
    private BigDecimal valor;
    private LocalDateTime data;
    private String descricao;

    public Transacao() {
    }

    public Transacao(int id, int idUsuario, int idCategoria, String tipo, BigDecimal valor, LocalDateTime data, String descricao) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.idCategoria = idCategoria;
        this.tipo = tipo;
        this.valor = valor;
        this.data = data;
        this.descricao = descricao;
    }

    // getters/setters minimais
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

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
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
        return "Transacao{id=" + id + ", idUsuario=" + idUsuario + ", idCategoria=" + idCategoria + ", tipo='" + tipo + "', valor=" + valor + ", data=" + data + ", descricao='" + descricao + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Transacao that = (Transacao) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
