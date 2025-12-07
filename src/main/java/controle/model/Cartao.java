package controle.model;

import java.time.LocalDateTime;

/**
 * Modelo para cartão de crédito/débito
 * Os dados sensíveis (número, CVV) são armazenados criptografados
 */
public class Cartao {

    private int id;
    private int idUsuario;
    private String token;           // Token único para identificar o cartão
    private String numeroMascarado; // Últimos 4 dígitos (**** **** **** 1234)
    private String numeroCripto;    // Número completo criptografado (AES-256)
    private String nomeTitular;
    private String validade;        // MM/YY
    private String cvvCripto;       // CVV criptografado
    private String bandeira;        // Visa, Mastercard, etc.
    private String tipo;            // CREDITO, DEBITO, AMBOS
    private String apelido;         // Nome amigável (ex: "Cartão Principal")
    private boolean ativo;
    private LocalDateTime dataCadastro;
    private LocalDateTime ultimoUso;

    public Cartao() {
        this.ativo = true;
        this.dataCadastro = LocalDateTime.now();
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNumeroMascarado() { return numeroMascarado; }
    public void setNumeroMascarado(String numeroMascarado) { this.numeroMascarado = numeroMascarado; }

    public String getNumeroCripto() { return numeroCripto; }
    public void setNumeroCripto(String numeroCripto) { this.numeroCripto = numeroCripto; }

    public String getNomeTitular() { return nomeTitular; }
    public void setNomeTitular(String nomeTitular) { this.nomeTitular = nomeTitular; }

    public String getValidade() { return validade; }
    public void setValidade(String validade) { this.validade = validade; }

    public String getCvvCripto() { return cvvCripto; }
    public void setCvvCripto(String cvvCripto) { this.cvvCripto = cvvCripto; }

    public String getBandeira() { return bandeira; }
    public void setBandeira(String bandeira) { this.bandeira = bandeira; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getApelido() { return apelido; }
    public void setApelido(String apelido) { this.apelido = apelido; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public LocalDateTime getUltimoUso() { return ultimoUso; }
    public void setUltimoUso(LocalDateTime ultimoUso) { this.ultimoUso = ultimoUso; }

    /**
     * Retorna uma descrição amigável para exibição em combos
     */
    @Override
    public String toString() {
        String desc = apelido != null && !apelido.isEmpty() ? apelido : bandeira;
        return desc + " " + numeroMascarado + " (" + tipo + ")";
    }

    /**
     * Retorna descrição detalhada para exibição
     */
    public String getDescricaoCompleta() {
        return String.format("%s - %s %s | %s | Válido até: %s",
                apelido != null ? apelido : "Cartão",
                bandeira,
                numeroMascarado,
                tipo,
                validade);
    }
}
