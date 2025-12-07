package controle.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model para configuração de impostos da Reforma Tributária 2026
 * Baseado na EC 132/2023
 */
public class ImpostoConfig {
    
    private int id;
    private String codigo;
    private String nome;
    private String sigla;
    private BigDecimal aliquotaPadrao;
    private BigDecimal aliquotaReduzida;
    private boolean aliquotaZero;
    private String esfera; // FEDERAL, ESTADUAL, MUNICIPAL, DUAL
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFim;
    private String baseLegal;
    private boolean ativo;
    
    // Constantes para os novos impostos da Reforma Tributária
    public static final String IBS = "IBS";  // Imposto sobre Bens e Serviços
    public static final String CBS = "CBS";  // Contribuição sobre Bens e Serviços
    public static final String IS = "IS";    // Imposto Seletivo
    
    // Alíquotas padrão previstas (soma aproximada de 26.5%)
    public static final BigDecimal ALIQUOTA_IBS_PADRAO = new BigDecimal("0.15");    // 15%
    public static final BigDecimal ALIQUOTA_CBS_PADRAO = new BigDecimal("0.088");   // 8.8%
    public static final BigDecimal ALIQUOTA_IBS_REDUZIDA = new BigDecimal("0.075"); // 7.5%
    public static final BigDecimal ALIQUOTA_CBS_REDUZIDA = new BigDecimal("0.044"); // 4.4%
    
    public ImpostoConfig() {}
    
    public ImpostoConfig(String codigo, String nome, String sigla, BigDecimal aliquotaPadrao) {
        this.codigo = codigo;
        this.nome = nome;
        this.sigla = sigla;
        this.aliquotaPadrao = aliquotaPadrao;
        this.ativo = true;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getSigla() { return sigla; }
    public void setSigla(String sigla) { this.sigla = sigla; }
    
    public BigDecimal getAliquotaPadrao() { return aliquotaPadrao; }
    public void setAliquotaPadrao(BigDecimal aliquotaPadrao) { this.aliquotaPadrao = aliquotaPadrao; }
    
    public BigDecimal getAliquotaReduzida() { return aliquotaReduzida; }
    public void setAliquotaReduzida(BigDecimal aliquotaReduzida) { this.aliquotaReduzida = aliquotaReduzida; }
    
    public boolean isAliquotaZero() { return aliquotaZero; }
    public void setAliquotaZero(boolean aliquotaZero) { this.aliquotaZero = aliquotaZero; }
    
    public String getEsfera() { return esfera; }
    public void setEsfera(String esfera) { this.esfera = esfera; }
    
    public LocalDate getVigenciaInicio() { return vigenciaInicio; }
    public void setVigenciaInicio(LocalDate vigenciaInicio) { this.vigenciaInicio = vigenciaInicio; }
    
    public LocalDate getVigenciaFim() { return vigenciaFim; }
    public void setVigenciaFim(LocalDate vigenciaFim) { this.vigenciaFim = vigenciaFim; }
    
    public String getBaseLegal() { return baseLegal; }
    public void setBaseLegal(String baseLegal) { this.baseLegal = baseLegal; }
    
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    
    /**
     * Calcula o valor do imposto sobre uma base
     */
    public BigDecimal calcular(BigDecimal baseCalculo) {
        if (baseCalculo == null || aliquotaPadrao == null) {
            return BigDecimal.ZERO;
        }
        return baseCalculo.multiply(aliquotaPadrao).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Calcula com alíquota reduzida (itens essenciais)
     */
    public BigDecimal calcularReduzido(BigDecimal baseCalculo) {
        if (baseCalculo == null) return BigDecimal.ZERO;
        BigDecimal aliq = aliquotaReduzida != null ? aliquotaReduzida : aliquotaPadrao;
        return baseCalculo.multiply(aliq).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Retorna a alíquota em percentual (ex: 15.00 para 15%)
     */
    public BigDecimal getAliquotaPercentual() {
        return aliquotaPadrao != null ? 
            aliquotaPadrao.multiply(new BigDecimal("100")) : BigDecimal.ZERO;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) - %.2f%%", nome, sigla, 
            aliquotaPadrao.multiply(new BigDecimal("100")));
    }
}
