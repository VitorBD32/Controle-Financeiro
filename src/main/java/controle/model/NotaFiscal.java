package controle.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model para Nota Fiscal Eletrônica
 * Conforme padrões da Receita Federal e nova Reforma Tributária
 */
public class NotaFiscal {
    
    private int id;
    private String numeroNf;
    private String serie;
    private TipoNF tipoNf;
    
    // Dados do emitente
    private String emitenteCnpj;
    private String emitenteRazaoSocial;
    private String emitenteNomeFantasia;
    private String emitenteEndereco;
    private String emitenteCidade;
    private String emitenteUf;
    private String emitenteCep;
    private String emitenteIe;
    private String emitenteIm;
    
    // Dados do destinatário
    private String destinatarioCpfCnpj;
    private String destinatarioNome;
    private String destinatarioEndereco;
    private String destinatarioCidade;
    private String destinatarioUf;
    private String destinatarioCep;
    private String destinatarioEmail;
    private String destinatarioTelefone;
    
    // Valores
    private BigDecimal valorProdutos = BigDecimal.ZERO;
    private BigDecimal valorServicos = BigDecimal.ZERO;
    private BigDecimal valorDesconto = BigDecimal.ZERO;
    private BigDecimal valorFrete = BigDecimal.ZERO;
    private BigDecimal valorOutras = BigDecimal.ZERO;
    
    // Impostos - Nova Reforma Tributária
    private BigDecimal baseCalculoIbs = BigDecimal.ZERO;
    private BigDecimal aliquotaIbs = BigDecimal.ZERO;
    private BigDecimal valorIbs = BigDecimal.ZERO;
    
    private BigDecimal baseCalculoCbs = BigDecimal.ZERO;
    private BigDecimal aliquotaCbs = BigDecimal.ZERO;
    private BigDecimal valorCbs = BigDecimal.ZERO;
    
    private BigDecimal baseCalculoIs = BigDecimal.ZERO;
    private BigDecimal aliquotaIs = BigDecimal.ZERO;
    private BigDecimal valorIs = BigDecimal.ZERO;
    
    // Campos adicionados para cálculo automático de tributos via API
    private BigDecimal valorCBS = BigDecimal.ZERO;  // Contribuição sobre Bens e Serviços
    private BigDecimal valorIBS = BigDecimal.ZERO;  // Imposto sobre Bens e Serviços
    private BigDecimal valorIS = BigDecimal.ZERO;   // Imposto Seletivo
    private String ufEmitente;
    private String municipioEmitente;
    
    // Impostos legados (transição)
    private BigDecimal valorIcms = BigDecimal.ZERO;
    private BigDecimal valorIss = BigDecimal.ZERO;
    private BigDecimal valorPis = BigDecimal.ZERO;
    private BigDecimal valorCofins = BigDecimal.ZERO;
    
    private BigDecimal valorTotalImpostos = BigDecimal.ZERO;
    private BigDecimal valorTotalNf = BigDecimal.ZERO;
    
    // Controle
    private LocalDateTime dataEmissao;
    private LocalDateTime dataSaida;
    private String chaveAcesso;
    private String protocoloAutorizacao;
    private StatusNF status = StatusNF.PENDENTE;
    private String motivoCancelamento;
    
    // Referências
    private int idUsuario;
    private Integer idTransacao;
    
    public enum TipoNF {
        NFE("NF-e - Nota Fiscal Eletrônica"),
        NFCE("NFC-e - Nota Fiscal de Consumidor"),
        NFSE("NFS-e - Nota Fiscal de Serviços");
        
        private final String descricao;
        TipoNF(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }
    
    public enum StatusNF {
        PENDENTE, AUTORIZADA, CANCELADA, DENEGADA, INUTILIZADA
    }
    
    public NotaFiscal() {
        this.dataEmissao = LocalDateTime.now();
        this.serie = "1";
        this.tipoNf = TipoNF.NFE;
    }
    
    // Métodos de cálculo
    
    /**
     * Calcula todos os impostos da NF baseado na nova reforma tributária
     */
    public void calcularImpostos(BigDecimal aliqIbs, BigDecimal aliqCbs, BigDecimal aliqIs) {
        BigDecimal baseCalculo = getValorBaseCalculo();
        
        this.baseCalculoIbs = baseCalculo;
        this.aliquotaIbs = aliqIbs != null ? aliqIbs : new BigDecimal("0.15");
        this.valorIbs = baseCalculo.multiply(this.aliquotaIbs).setScale(2, BigDecimal.ROUND_HALF_UP);
        
        this.baseCalculoCbs = baseCalculo;
        this.aliquotaCbs = aliqCbs != null ? aliqCbs : new BigDecimal("0.088");
        this.valorCbs = baseCalculo.multiply(this.aliquotaCbs).setScale(2, BigDecimal.ROUND_HALF_UP);
        
        this.baseCalculoIs = baseCalculo;
        this.aliquotaIs = aliqIs != null ? aliqIs : BigDecimal.ZERO;
        this.valorIs = baseCalculo.multiply(this.aliquotaIs).setScale(2, BigDecimal.ROUND_HALF_UP);
        
        this.valorTotalImpostos = valorIbs.add(valorCbs).add(valorIs);
        this.valorTotalNf = baseCalculo;
    }
    
    /**
     * Calcula a base de cálculo dos impostos
     */
    public BigDecimal getValorBaseCalculo() {
        return valorProdutos.add(valorServicos)
            .add(valorFrete)
            .add(valorOutras)
            .subtract(valorDesconto);
    }
    
    /**
     * Gera a chave de acesso da NF-e (44 dígitos)
     * Formato: cUF + AAMM + CNPJ + mod + serie + nNF + tpEmis + cNF + cDV
     */
    public String gerarChaveAcesso() {
        StringBuilder chave = new StringBuilder();
        
        // cUF - Código da UF (2 dígitos)
        chave.append(getCodigoUF(emitenteUf));
        
        // AAMM - Ano e mês de emissão (4 dígitos)
        chave.append(String.format("%02d%02d", 
            dataEmissao.getYear() % 100, dataEmissao.getMonthValue()));
        
        // CNPJ do emitente (14 dígitos)
        chave.append(emitenteCnpj.replaceAll("[^0-9]", ""));
        
        // Modelo (2 dígitos) - 55 para NF-e
        chave.append("55");
        
        // Série (3 dígitos)
        chave.append(String.format("%03d", Integer.parseInt(serie)));
        
        // Número da NF (9 dígitos)
        chave.append(String.format("%09d", Integer.parseInt(numeroNf.replaceAll("[^0-9]", ""))));
        
        // Tipo de emissão (1 dígito) - 1 = Normal
        chave.append("1");
        
        // Código numérico aleatório (8 dígitos)
        chave.append(String.format("%08d", (int)(Math.random() * 100000000)));
        
        // Dígito verificador (será calculado)
        String chaveBase = chave.toString();
        int dv = calcularDigitoVerificador(chaveBase);
        chave.append(dv);
        
        this.chaveAcesso = chave.toString();
        return this.chaveAcesso;
    }
    
    private int calcularDigitoVerificador(String chave) {
        int[] pesos = {4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2, 
                       9, 8, 7, 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2, 9, 8, 7, 
                       6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 43 && i < chave.length(); i++) {
            soma += Character.getNumericValue(chave.charAt(i)) * pesos[i];
        }
        int resto = soma % 11;
        return (resto == 0 || resto == 1) ? 0 : 11 - resto;
    }
    
    private String getCodigoUF(String uf) {
        if (uf == null) return "22"; // PI padrão
        switch (uf.toUpperCase()) {
            case "AC": return "12"; case "AL": return "27"; case "AP": return "16";
            case "AM": return "13"; case "BA": return "29"; case "CE": return "23";
            case "DF": return "53"; case "ES": return "32"; case "GO": return "52";
            case "MA": return "21"; case "MT": return "51"; case "MS": return "50";
            case "MG": return "31"; case "PA": return "15"; case "PB": return "25";
            case "PR": return "41"; case "PE": return "26"; case "PI": return "22";
            case "RJ": return "33"; case "RN": return "24"; case "RS": return "43";
            case "RO": return "11"; case "RR": return "14"; case "SC": return "42";
            case "SP": return "35"; case "SE": return "28"; case "TO": return "17";
            default: return "22";
        }
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNumeroNf() { return numeroNf; }
    public void setNumeroNf(String numeroNf) { this.numeroNf = numeroNf; }
    
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    
    public TipoNF getTipoNf() { return tipoNf; }
    public void setTipoNf(TipoNF tipoNf) { this.tipoNf = tipoNf; }
    
    public String getEmitenteCnpj() { return emitenteCnpj; }
    public void setEmitenteCnpj(String emitenteCnpj) { this.emitenteCnpj = emitenteCnpj; }
    
    public String getEmitenteRazaoSocial() { return emitenteRazaoSocial; }
    public void setEmitenteRazaoSocial(String emitenteRazaoSocial) { this.emitenteRazaoSocial = emitenteRazaoSocial; }
    
    public String getEmitenteNomeFantasia() { return emitenteNomeFantasia; }
    public void setEmitenteNomeFantasia(String emitenteNomeFantasia) { this.emitenteNomeFantasia = emitenteNomeFantasia; }
    
    public String getEmitenteEndereco() { return emitenteEndereco; }
    public void setEmitenteEndereco(String emitenteEndereco) { this.emitenteEndereco = emitenteEndereco; }
    
    public String getEmitenteCidade() { return emitenteCidade; }
    public void setEmitenteCidade(String emitenteCidade) { this.emitenteCidade = emitenteCidade; }
    
    public String getEmitenteUf() { return emitenteUf; }
    public void setEmitenteUf(String emitenteUf) { this.emitenteUf = emitenteUf; }
    
    public String getEmitenteCep() { return emitenteCep; }
    public void setEmitenteCep(String emitenteCep) { this.emitenteCep = emitenteCep; }
    
    public String getEmitenteIe() { return emitenteIe; }
    public void setEmitenteIe(String emitenteIe) { this.emitenteIe = emitenteIe; }
    
    public String getEmitenteIm() { return emitenteIm; }
    public void setEmitenteIm(String emitenteIm) { this.emitenteIm = emitenteIm; }
    
    public String getDestinatarioCpfCnpj() { return destinatarioCpfCnpj; }
    public void setDestinatarioCpfCnpj(String destinatarioCpfCnpj) { this.destinatarioCpfCnpj = destinatarioCpfCnpj; }
    
    public String getDestinatarioNome() { return destinatarioNome; }
    public void setDestinatarioNome(String destinatarioNome) { this.destinatarioNome = destinatarioNome; }
    
    public String getDestinatarioEndereco() { return destinatarioEndereco; }
    public void setDestinatarioEndereco(String destinatarioEndereco) { this.destinatarioEndereco = destinatarioEndereco; }
    
    public String getDestinatarioCidade() { return destinatarioCidade; }
    public void setDestinatarioCidade(String destinatarioCidade) { this.destinatarioCidade = destinatarioCidade; }
    
    public String getDestinatarioUf() { return destinatarioUf; }
    public void setDestinatarioUf(String destinatarioUf) { this.destinatarioUf = destinatarioUf; }
    
    public String getDestinatarioCep() { return destinatarioCep; }
    public void setDestinatarioCep(String destinatarioCep) { this.destinatarioCep = destinatarioCep; }
    
    public String getDestinatarioEmail() { return destinatarioEmail; }
    public void setDestinatarioEmail(String destinatarioEmail) { this.destinatarioEmail = destinatarioEmail; }
    
    public String getDestinatarioTelefone() { return destinatarioTelefone; }
    public void setDestinatarioTelefone(String destinatarioTelefone) { this.destinatarioTelefone = destinatarioTelefone; }
    
    public BigDecimal getValorProdutos() { return valorProdutos; }
    public void setValorProdutos(BigDecimal valorProdutos) { this.valorProdutos = valorProdutos; }
    
    public BigDecimal getValorServicos() { return valorServicos; }
    public void setValorServicos(BigDecimal valorServicos) { this.valorServicos = valorServicos; }
    
    public BigDecimal getValorDesconto() { return valorDesconto; }
    public void setValorDesconto(BigDecimal valorDesconto) { this.valorDesconto = valorDesconto; }
    
    public BigDecimal getValorFrete() { return valorFrete; }
    public void setValorFrete(BigDecimal valorFrete) { this.valorFrete = valorFrete; }
    
    public BigDecimal getValorOutras() { return valorOutras; }
    public void setValorOutras(BigDecimal valorOutras) { this.valorOutras = valorOutras; }
    
    public BigDecimal getBaseCalculoIbs() { return baseCalculoIbs; }
    public void setBaseCalculoIbs(BigDecimal baseCalculoIbs) { this.baseCalculoIbs = baseCalculoIbs; }
    
    public BigDecimal getAliquotaIbs() { return aliquotaIbs; }
    public void setAliquotaIbs(BigDecimal aliquotaIbs) { this.aliquotaIbs = aliquotaIbs; }
    
    public BigDecimal getValorIbs() { return valorIbs; }
    public void setValorIbs(BigDecimal valorIbs) { this.valorIbs = valorIbs; }
    
    public BigDecimal getBaseCalculoCbs() { return baseCalculoCbs; }
    public void setBaseCalculoCbs(BigDecimal baseCalculoCbs) { this.baseCalculoCbs = baseCalculoCbs; }
    
    public BigDecimal getAliquotaCbs() { return aliquotaCbs; }
    public void setAliquotaCbs(BigDecimal aliquotaCbs) { this.aliquotaCbs = aliquotaCbs; }
    
    public BigDecimal getValorCbs() { return valorCbs; }
    public void setValorCbs(BigDecimal valorCbs) { this.valorCbs = valorCbs; }
    
    // Getters/Setters para novos campos de tributos calculados via API
    public BigDecimal getValorCBS() { return valorCBS; }
    public void setValorCBS(BigDecimal valorCBS) { this.valorCBS = valorCBS; }
    
    public BigDecimal getValorIBS() { return valorIBS; }
    public void setValorIBS(BigDecimal valorIBS) { this.valorIBS = valorIBS; }
    
    public BigDecimal getValorIS() { return valorIS; }
    public void setValorIS(BigDecimal valorIS) { this.valorIS = valorIS; }
    
    public String getUfEmitente() { return ufEmitente; }
    public void setUfEmitente(String ufEmitente) { this.ufEmitente = ufEmitente; }
    
    public String getMunicipioEmitente() { return municipioEmitente; }
    public void setMunicipioEmitente(String municipioEmitente) { this.municipioEmitente = municipioEmitente; }
    
    public BigDecimal getBaseCalculoIs() { return baseCalculoIs; }
    public void setBaseCalculoIs(BigDecimal baseCalculoIs) { this.baseCalculoIs = baseCalculoIs; }
    
    public BigDecimal getAliquotaIs() { return aliquotaIs; }
    public void setAliquotaIs(BigDecimal aliquotaIs) { this.aliquotaIs = aliquotaIs; }
    
    public BigDecimal getValorIs() { return valorIs; }
    public void setValorIs(BigDecimal valorIs) { this.valorIs = valorIs; }
    
    public BigDecimal getValorIcms() { return valorIcms; }
    public void setValorIcms(BigDecimal valorIcms) { this.valorIcms = valorIcms; }
    
    public BigDecimal getValorIss() { return valorIss; }
    public void setValorIss(BigDecimal valorIss) { this.valorIss = valorIss; }
    
    public BigDecimal getValorPis() { return valorPis; }
    public void setValorPis(BigDecimal valorPis) { this.valorPis = valorPis; }
    
    public BigDecimal getValorCofins() { return valorCofins; }
    public void setValorCofins(BigDecimal valorCofins) { this.valorCofins = valorCofins; }
    
    public BigDecimal getValorTotalImpostos() { return valorTotalImpostos; }
    public void setValorTotalImpostos(BigDecimal valorTotalImpostos) { this.valorTotalImpostos = valorTotalImpostos; }
    
    public BigDecimal getValorTotalNf() { return valorTotalNf; }
    public void setValorTotalNf(BigDecimal valorTotalNf) { this.valorTotalNf = valorTotalNf; }
    
    public LocalDateTime getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDateTime dataEmissao) { this.dataEmissao = dataEmissao; }
    
    public LocalDateTime getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDateTime dataSaida) { this.dataSaida = dataSaida; }
    
    public String getChaveAcesso() { return chaveAcesso; }
    public void setChaveAcesso(String chaveAcesso) { this.chaveAcesso = chaveAcesso; }
    
    public String getProtocoloAutorizacao() { return protocoloAutorizacao; }
    public void setProtocoloAutorizacao(String protocoloAutorizacao) { this.protocoloAutorizacao = protocoloAutorizacao; }
    
    public StatusNF getStatus() { return status; }
    public void setStatus(StatusNF status) { this.status = status; }
    
    public String getMotivoCancelamento() { return motivoCancelamento; }
    public void setMotivoCancelamento(String motivoCancelamento) { this.motivoCancelamento = motivoCancelamento; }
    
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    
    public Integer getIdTransacao() { return idTransacao; }
    public void setIdTransacao(Integer idTransacao) { this.idTransacao = idTransacao; }
}
