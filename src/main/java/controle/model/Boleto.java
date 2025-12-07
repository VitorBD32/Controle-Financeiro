package controle.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model para Boleto Bancário
 * Conforme padrões FEBRABAN e Banco Central do Brasil
 */
public class Boleto {
    
    private int id;
    
    // Dados do boleto (Padrão FEBRABAN)
    private String codigoBarras;      // 44 posições
    private String linhaDigitavel;    // 47 posições formatada
    private String nossoNumero;
    
    // Banco
    private String codigoBanco = "001";
    private String nomeBanco = "Banco do Brasil S.A.";
    private String agencia;
    private String conta;
    private String carteira = "17";
    private String convenio;
    
    // Beneficiário (quem recebe)
    private String beneficiarioCpfCnpj;
    private String beneficiarioNome;
    private String beneficiarioEndereco;
    private String beneficiarioCidadeUf;
    
    // Pagador (quem paga)
    private String pagadorCpfCnpj;
    private String pagadorNome;
    private String pagadorEndereco;
    private String pagadorCidadeUf;
    private String pagadorCep;
    
    // Valores
    private BigDecimal valorDocumento;
    private BigDecimal valorDesconto = BigDecimal.ZERO;
    private BigDecimal valorDeducao = BigDecimal.ZERO;
    private BigDecimal valorMora = BigDecimal.ZERO;
    private BigDecimal valorAcrescimo = BigDecimal.ZERO;
    private BigDecimal valorCobrado;
    
    // Datas
    private LocalDate dataDocumento;
    private LocalDate dataVencimento;
    private LocalDate dataProcessamento;
    private LocalDate dataPagamento;
    
    // Instruções
    private String instrucao1 = "Não receber após o vencimento";
    private String instrucao2 = "Multa de 2% após o vencimento";
    private String instrucao3 = "Juros de 1% ao mês";
    private String demonstrativo;
    
    // Controle
    private StatusBoleto status = StatusBoleto.GERADO;
    
    // Referências
    private int idUsuario;
    private Integer idNotaFiscal;
    private Integer idTransacao;
    
    public enum StatusBoleto {
        GERADO, ENVIADO, PAGO, VENCIDO, CANCELADO, PROTESTADO
    }
    
    public Boleto() {
        this.dataDocumento = LocalDate.now();
        this.dataProcessamento = LocalDate.now();
    }
    
    /**
     * Gera o código de barras do boleto (44 posições)
     * Formato: BBBMC.CCCCC CCCCC.CCCCCC CCCCC.CCCCCC C FFFF9999999999
     * B = Código do banco
     * M = Código da moeda (9 = Real)
     * C = Campo livre (definido pelo banco)
     * F = Fator de vencimento
     * 9 = Valor
     */
    public String gerarCodigoBarras() {
        StringBuilder cb = new StringBuilder();
        
        // Posições 1-3: Código do banco
        cb.append(String.format("%3s", codigoBanco).replace(' ', '0'));
        
        // Posição 4: Código da moeda (9 = Real)
        cb.append("9");
        
        // Posição 5: Dígito verificador (calculado depois)
        cb.append("0"); // Placeholder
        
        // Posições 6-9: Fator de vencimento
        cb.append(calcularFatorVencimento());
        
        // Posições 10-19: Valor (10 dígitos sem pontuação)
        String valorStr = valorDocumento.multiply(new BigDecimal("100"))
            .setScale(0, BigDecimal.ROUND_DOWN).toString();
        cb.append(String.format("%10s", valorStr).replace(' ', '0'));
        
        // Posições 20-44: Campo livre (25 dígitos - específico do banco)
        cb.append(gerarCampoLivre());
        
        // Calcular e inserir DV na posição 5
        String codigoSemDv = cb.substring(0, 4) + cb.substring(5);
        int dv = calcularDVCodigoBarras(codigoSemDv);
        cb.setCharAt(4, Character.forDigit(dv, 10));
        
        this.codigoBarras = cb.toString();
        return this.codigoBarras;
    }
    
    /**
     * Gera a linha digitável a partir do código de barras
     * Formato: BBBMC.CCCCD CCCCC.CCCCCD CCCCC.CCCCCD D FFFFVVVVVVVVVV
     */
    public String gerarLinhaDigitavel() {
        if (codigoBarras == null || codigoBarras.length() != 44) {
            gerarCodigoBarras();
        }
        
        StringBuilder ld = new StringBuilder();
        
        // Campo 1: Banco + Moeda + 5 primeiros do campo livre + DV
        String campo1 = codigoBarras.substring(0, 4) + codigoBarras.substring(19, 24);
        int dv1 = calcularDVMod10(campo1);
        ld.append(campo1.substring(0, 5)).append(".").append(campo1.substring(5)).append(dv1).append(" ");
        
        // Campo 2: Posições 25-34 do código + DV
        String campo2 = codigoBarras.substring(24, 34);
        int dv2 = calcularDVMod10(campo2);
        ld.append(campo2.substring(0, 5)).append(".").append(campo2.substring(5)).append(dv2).append(" ");
        
        // Campo 3: Posições 35-44 do código + DV
        String campo3 = codigoBarras.substring(34, 44);
        int dv3 = calcularDVMod10(campo3);
        ld.append(campo3.substring(0, 5)).append(".").append(campo3.substring(5)).append(dv3).append(" ");
        
        // Campo 4: DV geral (posição 5 do código de barras)
        ld.append(codigoBarras.charAt(4)).append(" ");
        
        // Campo 5: Fator vencimento + Valor
        ld.append(codigoBarras.substring(5, 19));
        
        this.linhaDigitavel = ld.toString();
        return this.linhaDigitavel;
    }
    
    /**
     * Calcula o fator de vencimento (dias desde 07/10/1997)
     */
    private String calcularFatorVencimento() {
        LocalDate dataBase = LocalDate.of(1997, 10, 7);
        long dias = java.time.temporal.ChronoUnit.DAYS.between(dataBase, dataVencimento);
        
        // Após 21/02/2025, o fator reinicia do 1000
        if (dias > 9999) {
            dias = 1000 + (dias - 10000);
        }
        
        return String.format("%04d", dias);
    }
    
    /**
     * Gera o campo livre (específico do Banco do Brasil - Carteira 17)
     * 25 posições
     */
    private String gerarCampoLivre() {
        StringBuilder cl = new StringBuilder();
        
        // Convênio (7 dígitos)
        String conv = convenio != null ? convenio : "0000000";
        cl.append(String.format("%7s", conv.replaceAll("[^0-9]", "")).replace(' ', '0'));
        
        // Nosso número (10 dígitos)
        String nn = nossoNumero != null ? nossoNumero : "0000000000";
        cl.append(String.format("%10s", nn.replaceAll("[^0-9]", "")).replace(' ', '0'));
        
        // Carteira (2 dígitos)
        cl.append(String.format("%2s", carteira).replace(' ', '0'));
        
        // Agência (4 dígitos)
        String ag = agencia != null ? agencia.replaceAll("[^0-9]", "") : "0000";
        cl.append(String.format("%4s", ag).replace(' ', '0').substring(0, 4));
        
        // Conta (2 últimos dígitos ou zeros)
        String ct = conta != null ? conta.replaceAll("[^0-9]", "") : "00";
        cl.append(ct.length() >= 2 ? ct.substring(ct.length() - 2) : "00");
        
        return cl.toString().substring(0, 25);
    }
    
    /**
     * Calcula DV do código de barras (módulo 11)
     */
    private int calcularDVCodigoBarras(String codigo) {
        int[] pesos = {4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2, 
                       9, 8, 7, 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2, 9, 8, 7, 
                       6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < codigo.length() && i < 43; i++) {
            soma += Character.getNumericValue(codigo.charAt(i)) * pesos[i];
        }
        int resto = soma % 11;
        int dv = 11 - resto;
        if (dv == 0 || dv == 10 || dv == 11) dv = 1;
        return dv;
    }
    
    /**
     * Calcula DV módulo 10 para linha digitável
     */
    private int calcularDVMod10(String campo) {
        int soma = 0;
        int peso = 2;
        for (int i = campo.length() - 1; i >= 0; i--) {
            int produto = Character.getNumericValue(campo.charAt(i)) * peso;
            soma += (produto > 9) ? (produto - 9) : produto;
            peso = (peso == 2) ? 1 : 2;
        }
        int resto = soma % 10;
        return (resto == 0) ? 0 : (10 - resto);
    }
    
    /**
     * Gera o nosso número sequencial
     */
    public void gerarNossoNumero(int sequencial) {
        this.nossoNumero = String.format("%010d", sequencial);
    }
    
    /**
     * Calcula valor total a cobrar
     */
    public BigDecimal calcularValorCobrado() {
        this.valorCobrado = valorDocumento
            .subtract(valorDesconto)
            .subtract(valorDeducao)
            .add(valorMora)
            .add(valorAcrescimo);
        return valorCobrado;
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    
    public String getLinhaDigitavel() { return linhaDigitavel; }
    public void setLinhaDigitavel(String linhaDigitavel) { this.linhaDigitavel = linhaDigitavel; }
    
    public String getNossoNumero() { return nossoNumero; }
    public void setNossoNumero(String nossoNumero) { this.nossoNumero = nossoNumero; }
    
    public String getCodigoBanco() { return codigoBanco; }
    public void setCodigoBanco(String codigoBanco) { this.codigoBanco = codigoBanco; }
    
    public String getNomeBanco() { return nomeBanco; }
    public void setNomeBanco(String nomeBanco) { this.nomeBanco = nomeBanco; }
    
    public String getAgencia() { return agencia; }
    public void setAgencia(String agencia) { this.agencia = agencia; }
    
    public String getConta() { return conta; }
    public void setConta(String conta) { this.conta = conta; }
    
    public String getCarteira() { return carteira; }
    public void setCarteira(String carteira) { this.carteira = carteira; }
    
    public String getConvenio() { return convenio; }
    public void setConvenio(String convenio) { this.convenio = convenio; }
    
    public String getBeneficiarioCpfCnpj() { return beneficiarioCpfCnpj; }
    public void setBeneficiarioCpfCnpj(String beneficiarioCpfCnpj) { this.beneficiarioCpfCnpj = beneficiarioCpfCnpj; }
    
    public String getBeneficiarioNome() { return beneficiarioNome; }
    public void setBeneficiarioNome(String beneficiarioNome) { this.beneficiarioNome = beneficiarioNome; }
    
    public String getBeneficiarioEndereco() { return beneficiarioEndereco; }
    public void setBeneficiarioEndereco(String beneficiarioEndereco) { this.beneficiarioEndereco = beneficiarioEndereco; }
    
    public String getBeneficiarioCidadeUf() { return beneficiarioCidadeUf; }
    public void setBeneficiarioCidadeUf(String beneficiarioCidadeUf) { this.beneficiarioCidadeUf = beneficiarioCidadeUf; }
    
    public String getPagadorCpfCnpj() { return pagadorCpfCnpj; }
    public void setPagadorCpfCnpj(String pagadorCpfCnpj) { this.pagadorCpfCnpj = pagadorCpfCnpj; }
    
    public String getPagadorNome() { return pagadorNome; }
    public void setPagadorNome(String pagadorNome) { this.pagadorNome = pagadorNome; }
    
    public String getPagadorEndereco() { return pagadorEndereco; }
    public void setPagadorEndereco(String pagadorEndereco) { this.pagadorEndereco = pagadorEndereco; }
    
    public String getPagadorCidadeUf() { return pagadorCidadeUf; }
    public void setPagadorCidadeUf(String pagadorCidadeUf) { this.pagadorCidadeUf = pagadorCidadeUf; }
    
    public String getPagadorCep() { return pagadorCep; }
    public void setPagadorCep(String pagadorCep) { this.pagadorCep = pagadorCep; }
    
    public BigDecimal getValorDocumento() { return valorDocumento; }
    public void setValorDocumento(BigDecimal valorDocumento) { this.valorDocumento = valorDocumento; }
    
    public BigDecimal getValorDesconto() { return valorDesconto; }
    public void setValorDesconto(BigDecimal valorDesconto) { this.valorDesconto = valorDesconto; }
    
    public BigDecimal getValorDeducao() { return valorDeducao; }
    public void setValorDeducao(BigDecimal valorDeducao) { this.valorDeducao = valorDeducao; }
    
    public BigDecimal getValorMora() { return valorMora; }
    public void setValorMora(BigDecimal valorMora) { this.valorMora = valorMora; }
    
    public BigDecimal getValorAcrescimo() { return valorAcrescimo; }
    public void setValorAcrescimo(BigDecimal valorAcrescimo) { this.valorAcrescimo = valorAcrescimo; }
    
    public BigDecimal getValorCobrado() { return valorCobrado; }
    public void setValorCobrado(BigDecimal valorCobrado) { this.valorCobrado = valorCobrado; }
    
    public LocalDate getDataDocumento() { return dataDocumento; }
    public void setDataDocumento(LocalDate dataDocumento) { this.dataDocumento = dataDocumento; }
    
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    
    public LocalDate getDataProcessamento() { return dataProcessamento; }
    public void setDataProcessamento(LocalDate dataProcessamento) { this.dataProcessamento = dataProcessamento; }
    
    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
    
    public String getInstrucao1() { return instrucao1; }
    public void setInstrucao1(String instrucao1) { this.instrucao1 = instrucao1; }
    
    public String getInstrucao2() { return instrucao2; }
    public void setInstrucao2(String instrucao2) { this.instrucao2 = instrucao2; }
    
    public String getInstrucao3() { return instrucao3; }
    public void setInstrucao3(String instrucao3) { this.instrucao3 = instrucao3; }
    
    public String getDemonstrativo() { return demonstrativo; }
    public void setDemonstrativo(String demonstrativo) { this.demonstrativo = demonstrativo; }
    
    public StatusBoleto getStatus() { return status; }
    public void setStatus(StatusBoleto status) { this.status = status; }
    
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    
    public Integer getIdNotaFiscal() { return idNotaFiscal; }
    public void setIdNotaFiscal(Integer idNotaFiscal) { this.idNotaFiscal = idNotaFiscal; }
    
    public Integer getIdTransacao() { return idTransacao; }
    public void setIdTransacao(Integer idTransacao) { this.idTransacao = idTransacao; }
}
