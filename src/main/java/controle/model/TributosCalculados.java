package controle.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO para resultado de cálculo de tributos
 * Armazena valores de CBS, IBS e IS calculados
 * 
 * Baseado na Reforma Tributária 2026 (EC 132/2023)
 * 
 * @author Sistema de Controle Financeiro
 * @version 1.0
 */
public class TributosCalculados {
    
    private BigDecimal valorBase;
    private BigDecimal valorCBS;    // Contribuição sobre Bens e Serviços (Federal)
    private BigDecimal valorIBS;    // Imposto sobre Bens e Serviços (Estadual/Municipal)
    private BigDecimal valorIS;     // Imposto Seletivo (produtos específicos)
    private BigDecimal totalTributos;
    private BigDecimal valorLiquido;
    
    private String uf;
    private String municipio;
    
    // Alíquotas aplicadas
    private BigDecimal aliquotaCBS;
    private BigDecimal aliquotaIBS;
    private BigDecimal aliquotaIS;
    
    public TributosCalculados() {
        this.valorBase = BigDecimal.ZERO;
        this.valorCBS = BigDecimal.ZERO;
        this.valorIBS = BigDecimal.ZERO;
        this.valorIS = BigDecimal.ZERO;
        recalcularTotais();
    }
    
    public TributosCalculados(BigDecimal valorBase, BigDecimal valorCBS, BigDecimal valorIBS, BigDecimal valorIS, String uf, String municipio) {
        this.valorBase = valorBase != null ? valorBase : BigDecimal.ZERO;
        this.valorCBS = valorCBS != null ? valorCBS : BigDecimal.ZERO;
        this.valorIBS = valorIBS != null ? valorIBS : BigDecimal.ZERO;
        this.valorIS = valorIS != null ? valorIS : BigDecimal.ZERO;
        this.uf = uf;
        this.municipio = municipio;
        recalcularTotais();
    }
    
    /**
     * Recalcula total de tributos e valor líquido
     */
    private void recalcularTotais() {
        this.totalTributos = valorCBS.add(valorIBS).add(valorIS).setScale(2, RoundingMode.HALF_UP);
        this.valorLiquido = valorBase.subtract(totalTributos).setScale(2, RoundingMode.HALF_UP);
    }
    
    // Getters e Setters
    
    public BigDecimal getValorBase() {
        return valorBase;
    }
    
    public void setValorBase(BigDecimal valorBase) {
        this.valorBase = valorBase;
        recalcularTotais();
    }
    
    public BigDecimal getValorCBS() {
        return valorCBS;
    }
    
    public void setValorCBS(BigDecimal valorCBS) {
        this.valorCBS = valorCBS;
        recalcularTotais();
    }
    
    public BigDecimal getValorIBS() {
        return valorIBS;
    }
    
    public void setValorIBS(BigDecimal valorIBS) {
        this.valorIBS = valorIBS;
        recalcularTotais();
    }
    
    public BigDecimal getValorIS() {
        return valorIS;
    }
    
    public void setValorIS(BigDecimal valorIS) {
        this.valorIS = valorIS;
        recalcularTotais();
    }
    
    public BigDecimal getTotalTributos() {
        return totalTributos;
    }
    
    public BigDecimal getValorLiquido() {
        return valorLiquido;
    }
    
    public String getUf() {
        return uf;
    }
    
    public void setUf(String uf) {
        this.uf = uf;
    }
    
    public String getMunicipio() {
        return municipio;
    }
    
    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }
    
    public BigDecimal getAliquotaCBS() {
        return aliquotaCBS;
    }
    
    public void setAliquotaCBS(BigDecimal aliquotaCBS) {
        this.aliquotaCBS = aliquotaCBS;
    }
    
    public BigDecimal getAliquotaIBS() {
        return aliquotaIBS;
    }
    
    public void setAliquotaIBS(BigDecimal aliquotaIBS) {
        this.aliquotaIBS = aliquotaIBS;
    }
    
    public BigDecimal getAliquotaIS() {
        return aliquotaIS;
    }
    
    public void setAliquotaIS(BigDecimal aliquotaIS) {
        this.aliquotaIS = aliquotaIS;
    }
    
    /**
     * Retorna percentual total de tributos sobre valor base
     */
    public BigDecimal getPercentualTotal() {
        if (valorBase == null || valorBase.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalTributos.divide(valorBase, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"))
                            .setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String toString() {
        return String.format("TributosCalculados[Base=R$ %.2f, CBS=R$ %.2f, IBS=R$ %.2f, IS=R$ %.2f, Total=R$ %.2f (%.2f%%), Líquido=R$ %.2f]",
                valorBase, valorCBS, valorIBS, valorIS, totalTributos, getPercentualTotal(), valorLiquido);
    }
}
