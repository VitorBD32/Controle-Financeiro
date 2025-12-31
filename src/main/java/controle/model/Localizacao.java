package controle.model;

import java.time.LocalDateTime;

/**
 * Modelo de dados para Localização Geográfica
 * Armazena informações de latitude, longitude, cidade, UF, etc.
 * 
 * @author Sistema de Controle Financeiro
 * @version 1.0
 */
public class Localizacao {
    
    private String ip;
    private double latitude;
    private double longitude;
    private String cidade;
    private String regiao;
    private String uf;
    private String pais;
    private String codigoPais;
    private String timezone;
    private String fonte;  // "ipapi.co", "ip-api.com", "manual", etc.
    private LocalDateTime dataDeteccao;
    
    public Localizacao() {
        this.dataDeteccao = LocalDateTime.now();
    }
    
    public Localizacao(String cidade, String uf) {
        this();
        this.cidade = cidade;
        this.uf = uf;
        this.pais = "Brasil";
        this.codigoPais = "BR";
        this.fonte = "manual";
    }

    // Getters e Setters
    
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) {
        this.regiao = regiao;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCodigoPais() {
        return codigoPais;
    }

    public void setCodigoPais(String codigoPais) {
        this.codigoPais = codigoPais;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getFonte() {
        return fonte;
    }

    public void setFonte(String fonte) {
        this.fonte = fonte;
    }

    public LocalDateTime getDataDeteccao() {
        return dataDeteccao;
    }

    public void setDataDeteccao(LocalDateTime dataDeteccao) {
        this.dataDeteccao = dataDeteccao;
    }
    
    /**
     * Retorna descrição completa da localização
     */
    public String getDescricaoCompleta() {
        return String.format("%s, %s - %s (%.4f, %.4f)", 
                           cidade, uf, pais, latitude, longitude);
    }
    
    /**
     * Retorna descrição resumida (Cidade, UF)
     */
    public String getDescricaoResumida() {
        return String.format("%s, %s", cidade, uf);
    }
    
    /**
     * Verifica se a localização é válida
     */
    public boolean isValida() {
        return cidade != null && !cidade.isEmpty() && 
               uf != null && !uf.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("Localizacao{cidade='%s', uf='%s', lat=%.4f, lon=%.4f, fonte='%s'}", 
                           cidade, uf, latitude, longitude, fonte);
    }
}
