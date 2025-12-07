package controle.util;

import controle.model.Boleto;
import controle.model.NotaFiscal;
import controle.model.Usuario;
import controle.model.Transacao;
import controle.dao.SistemaConfigDAO;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Gerador de Boletos e Notas Fiscais em formato imprimível
 * Conforme normas FEBRABAN, Banco Central, Receita Federal e Estadual
 * Adaptado para a Reforma Tributária 2026 (EC 132/2023)
 */
public class DocumentoFiscalGenerator {
    
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private final SistemaConfigDAO configDAO = new SistemaConfigDAO();
    
    // Cores padrão
    private static final Color AZUL_BANCO = new Color(0, 51, 102);
    private static final Color CINZA_CLARO = new Color(240, 240, 240);
    private static final Color LINHA = new Color(200, 200, 200);
    
    /**
     * Gera um boleto bancário para impressão
     */
    public BufferedImage gerarImagemBoleto(Boleto boleto) {
        int width = 800;
        int height = 400;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        
        // Configurações de renderização
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        
        // Fundo branco
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        
        int y = 10;
        
        // ============ CABEÇALHO DO BANCO ============
        g2.setColor(AZUL_BANCO);
        g2.fillRect(10, y, width - 20, 40);
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString(boleto.getCodigoBanco() + " - " + boleto.getNomeBanco(), 20, y + 27);
        
        // Código do banco com barra
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString(boleto.getCodigoBanco() + "-X", width - 80, y + 27);
        
        y += 50;
        
        // ============ LINHA DIGITÁVEL ============
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Courier New", Font.BOLD, 14));
        String linhaDigitavel = boleto.getLinhaDigitavel() != null ? 
            boleto.getLinhaDigitavel() : boleto.gerarLinhaDigitavel();
        g2.drawString(linhaDigitavel, 20, y);
        
        y += 20;
        
        // Linha divisória
        g2.setColor(LINHA);
        g2.drawLine(10, y, width - 10, y);
        
        y += 15;
        
        // ============ DADOS DO BENEFICIÁRIO ============
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, 9));
        g2.drawString("Local de Pagamento", 20, y);
        g2.drawString("Vencimento", width - 150, y);
        
        y += 12;
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("PAGÁVEL EM QUALQUER BANCO ATÉ O VENCIMENTO", 20, y);
        
        LocalDate venc = boleto.getDataVencimento();
        g2.drawString(venc != null ? venc.format(DATE_FORMAT) : "", width - 150, y);
        
        y += 20;
        g2.setColor(LINHA);
        g2.drawLine(10, y, width - 10, y);
        
        y += 15;
        
        // Beneficiário
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, 9));
        g2.drawString("Beneficiário", 20, y);
        g2.drawString("Agência/Código do Beneficiário", width - 200, y);
        
        y += 12;
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString(boleto.getBeneficiarioNome() + " - CNPJ: " + boleto.getBeneficiarioCpfCnpj(), 20, y);
        g2.drawString(boleto.getAgencia() + " / " + boleto.getConta(), width - 200, y);
        
        y += 20;
        g2.setColor(LINHA);
        g2.drawLine(10, y, width - 10, y);
        
        y += 15;
        
        // Data documento, número documento, espécie, aceite
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, 9));
        g2.drawString("Data do Documento", 20, y);
        g2.drawString("Nº do Documento", 150, y);
        g2.drawString("Espécie Doc.", 300, y);
        g2.drawString("Aceite", 400, y);
        g2.drawString("Data Processamento", 480, y);
        g2.drawString("Nosso Número", width - 150, y);
        
        y += 12;
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        LocalDate dataDoc = boleto.getDataDocumento();
        g2.drawString(dataDoc != null ? dataDoc.format(DATE_FORMAT) : "", 20, y);
        g2.drawString(boleto.getNossoNumero() != null ? boleto.getNossoNumero() : "", 150, y);
        g2.drawString("DM", 300, y);
        g2.drawString("N", 400, y);
        LocalDate dataProc = boleto.getDataProcessamento();
        g2.drawString(dataProc != null ? dataProc.format(DATE_FORMAT) : "", 480, y);
        g2.drawString(boleto.getNossoNumero() != null ? boleto.getNossoNumero() : "", width - 150, y);
        
        y += 20;
        g2.setColor(LINHA);
        g2.drawLine(10, y, width - 10, y);
        
        y += 15;
        
        // Valores
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, 9));
        g2.drawString("Uso do Banco", 20, y);
        g2.drawString("Carteira", 100, y);
        g2.drawString("Espécie", 180, y);
        g2.drawString("Quantidade", 260, y);
        g2.drawString("(x) Valor", 360, y);
        g2.drawString("(=) Valor do Documento", width - 180, y);
        
        y += 12;
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("", 20, y);
        g2.drawString(boleto.getCarteira(), 100, y);
        g2.drawString("R$", 180, y);
        g2.drawString("", 260, y);
        g2.drawString("", 360, y);
        BigDecimal valor = boleto.getValorDocumento();
        g2.drawString(valor != null ? CURRENCY_FORMAT.format(valor) : "", width - 180, y);
        
        y += 20;
        g2.setColor(LINHA);
        g2.drawLine(10, y, width - 10, y);
        
        y += 15;
        
        // Instruções e descontos
        int xInstrucoes = 20;
        int xValores = width - 200;
        
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, 9));
        g2.drawString("Instruções (Texto de responsabilidade do Beneficiário)", xInstrucoes, y);
        g2.drawString("(-) Desconto/Abatimento", xValores, y);
        
        y += 15;
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        if (boleto.getInstrucao1() != null) g2.drawString(boleto.getInstrucao1(), xInstrucoes, y);
        g2.drawString(CURRENCY_FORMAT.format(boleto.getValorDesconto()), xValores, y);
        
        y += 12;
        if (boleto.getInstrucao2() != null) g2.drawString(boleto.getInstrucao2(), xInstrucoes, y);
        g2.setFont(new Font("Arial", Font.PLAIN, 9));
        g2.drawString("(+) Mora/Multa", xValores, y);
        
        y += 12;
        if (boleto.getInstrucao3() != null) g2.drawString(boleto.getInstrucao3(), xInstrucoes, y);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString(CURRENCY_FORMAT.format(boleto.getValorMora()), xValores, y);
        
        y += 20;
        g2.setFont(new Font("Arial", Font.PLAIN, 9));
        g2.drawString("(=) Valor Cobrado", xValores, y);
        y += 12;
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        BigDecimal valorCobrado = boleto.calcularValorCobrado();
        g2.drawString(CURRENCY_FORMAT.format(valorCobrado), xValores, y);
        
        y += 25;
        g2.setColor(LINHA);
        g2.drawLine(10, y, width - 10, y);
        
        y += 15;
        
        // Pagador
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, 9));
        g2.drawString("Pagador:", 20, y);
        
        y += 12;
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString(boleto.getPagadorNome() + " - CPF/CNPJ: " + boleto.getPagadorCpfCnpj(), 20, y);
        
        y += 12;
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString(boleto.getPagadorEndereco() + " - " + boleto.getPagadorCidadeUf() + " - CEP: " + boleto.getPagadorCep(), 20, y);
        
        y += 25;
        
        // Código de barras (representação visual simplificada)
        desenharCodigoBarras(g2, boleto.getCodigoBarras() != null ? boleto.getCodigoBarras() : boleto.gerarCodigoBarras(), 
                            20, y, width - 40, 40);
        
        g2.dispose();
        return image;
    }
    
    /**
     * Desenha uma representação visual do código de barras
     */
    private void desenharCodigoBarras(Graphics2D g2, String codigo, int x, int y, int width, int height) {
        g2.setColor(Color.BLACK);
        
        if (codigo == null || codigo.isEmpty()) {
            g2.drawRect(x, y, width, height);
            g2.drawString("CÓDIGO DE BARRAS", x + width/2 - 50, y + height/2);
            return;
        }
        
        int barWidth = width / codigo.length();
        for (int i = 0; i < codigo.length(); i++) {
            int digit = Character.getNumericValue(codigo.charAt(i));
            if (digit % 2 == 0) {
                g2.fillRect(x + i * barWidth, y, barWidth, height);
            } else {
                g2.fillRect(x + i * barWidth, y, barWidth / 2, height);
            }
        }
    }
    
    /**
     * Gera uma imagem da Nota Fiscal
     */
    public BufferedImage gerarImagemNotaFiscal(NotaFiscal nf) {
        int width = 800;
        int height = 700;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        
        // Fundo branco
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        
        int y = 15;
        int margin = 20;
        
        // ============ CABEÇALHO DA NF ============
        g2.setColor(AZUL_BANCO);
        g2.fillRect(margin, y, width - 2*margin, 60);
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("NOTA FISCAL ELETRÔNICA - " + nf.getTipoNf().getDescricao(), margin + 10, y + 25);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString("Nº " + nf.getNumeroNf() + " - Série " + nf.getSerie(), margin + 10, y + 45);
        
        // Data de emissão no canto direito
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        String dataEmissao = nf.getDataEmissao() != null ? 
            nf.getDataEmissao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
        g2.drawString("Emissão: " + dataEmissao, width - margin - 180, y + 35);
        
        y += 70;
        
        // ============ CHAVE DE ACESSO ============
        g2.setColor(CINZA_CLARO);
        g2.fillRect(margin, y, width - 2*margin, 35);
        
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, 9));
        g2.drawString("CHAVE DE ACESSO", margin + 5, y + 12);
        
        g2.setFont(new Font("Courier New", Font.BOLD, 11));
        String chave = nf.getChaveAcesso() != null ? nf.getChaveAcesso() : nf.gerarChaveAcesso();
        // Formatar chave em grupos de 4
        String chaveFormatada = chave.replaceAll("(.{4})", "$1 ");
        g2.drawString(chaveFormatada, margin + 5, y + 28);
        
        y += 45;
        
        // ============ DADOS DO EMITENTE ============
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString("EMITENTE", margin, y);
        
        y += 15;
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString("Razão Social: " + nf.getEmitenteRazaoSocial(), margin, y);
        y += 12;
        g2.drawString("CNPJ: " + nf.getEmitenteCnpj() + "  |  IE: " + nf.getEmitenteIe() + "  |  IM: " + nf.getEmitenteIm(), margin, y);
        y += 12;
        g2.drawString("Endereço: " + nf.getEmitenteEndereco() + " - " + nf.getEmitenteCidade() + "/" + nf.getEmitenteUf() + " - CEP: " + nf.getEmitenteCep(), margin, y);
        
        y += 25;
        g2.setColor(LINHA);
        g2.drawLine(margin, y, width - margin, y);
        y += 15;
        
        // ============ DADOS DO DESTINATÁRIO ============
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString("DESTINATÁRIO", margin, y);
        
        y += 15;
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString("Nome/Razão Social: " + nf.getDestinatarioNome(), margin, y);
        y += 12;
        g2.drawString("CPF/CNPJ: " + nf.getDestinatarioCpfCnpj(), margin, y);
        y += 12;
        g2.drawString("Endereço: " + (nf.getDestinatarioEndereco() != null ? nf.getDestinatarioEndereco() : "") + 
                      " - " + (nf.getDestinatarioCidade() != null ? nf.getDestinatarioCidade() : "") + 
                      "/" + (nf.getDestinatarioUf() != null ? nf.getDestinatarioUf() : ""), margin, y);
        
        y += 25;
        g2.setColor(LINHA);
        g2.drawLine(margin, y, width - margin, y);
        y += 15;
        
        // ============ VALORES DA NOTA ============
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString("VALORES", margin, y);
        
        y += 20;
        
        // Tabela de valores
        int col1 = margin;
        int col2 = 250;
        int col3 = 450;
        int col4 = width - margin - 100;
        
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        
        g2.drawString("Valor dos Produtos:", col1, y);
        g2.drawString(CURRENCY_FORMAT.format(nf.getValorProdutos()), col2, y);
        g2.drawString("Valor dos Serviços:", col3, y);
        g2.drawString(CURRENCY_FORMAT.format(nf.getValorServicos()), col4, y);
        
        y += 15;
        g2.drawString("Valor do Frete:", col1, y);
        g2.drawString(CURRENCY_FORMAT.format(nf.getValorFrete()), col2, y);
        g2.drawString("Descontos:", col3, y);
        g2.drawString(CURRENCY_FORMAT.format(nf.getValorDesconto()), col4, y);
        
        y += 25;
        g2.setColor(LINHA);
        g2.drawLine(margin, y, width - margin, y);
        y += 15;
        
        // ============ TRIBUTOS - NOVA REFORMA TRIBUTÁRIA ============
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString("TRIBUTOS - REFORMA TRIBUTÁRIA 2026 (EC 132/2023)", margin, y);
        
        y += 5;
        g2.setColor(CINZA_CLARO);
        g2.fillRect(margin, y, width - 2*margin, 80);
        
        y += 20;
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        
        // IBS
        g2.drawString("IBS (Imposto sobre Bens e Serviços):", col1, y);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString("Base: " + CURRENCY_FORMAT.format(nf.getBaseCalculoIbs()), col2, y);
        g2.drawString("Alíquota: " + nf.getAliquotaIbs().multiply(new BigDecimal("100")).setScale(2) + "%", col3, y);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("Valor: " + CURRENCY_FORMAT.format(nf.getValorIbs()), col4, y);
        
        y += 18;
        // CBS
        g2.drawString("CBS (Contribuição sobre Bens e Serviços):", col1, y);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString("Base: " + CURRENCY_FORMAT.format(nf.getBaseCalculoCbs()), col2, y);
        g2.drawString("Alíquota: " + nf.getAliquotaCbs().multiply(new BigDecimal("100")).setScale(2) + "%", col3, y);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("Valor: " + CURRENCY_FORMAT.format(nf.getValorCbs()), col4, y);
        
        y += 18;
        // IS (se houver)
        if (nf.getValorIs().compareTo(BigDecimal.ZERO) > 0) {
            g2.drawString("IS (Imposto Seletivo):", col1, y);
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            g2.drawString("Base: " + CURRENCY_FORMAT.format(nf.getBaseCalculoIs()), col2, y);
            g2.drawString("Alíquota: " + nf.getAliquotaIs().multiply(new BigDecimal("100")).setScale(2) + "%", col3, y);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.drawString("Valor: " + CURRENCY_FORMAT.format(nf.getValorIs()), col4, y);
            y += 18;
        }
        
        y += 10;
        g2.setColor(LINHA);
        g2.drawLine(margin, y, width - margin, y);
        y += 15;
        
        // ============ TOTAL DE TRIBUTOS ============
        g2.setColor(new Color(255, 243, 205)); // Amarelo claro
        g2.fillRect(margin, y, width - 2*margin, 30);
        
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString("TOTAL DE TRIBUTOS (Lei 12.741/2012):", col1, y + 20);
        g2.drawString(CURRENCY_FORMAT.format(nf.getValorTotalImpostos()), col4, y + 20);
        
        // Percentual sobre o valor
        BigDecimal percentual = nf.getValorTotalNf().compareTo(BigDecimal.ZERO) > 0 ?
            nf.getValorTotalImpostos().divide(nf.getValorTotalNf(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100")) : BigDecimal.ZERO;
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString("(" + percentual.setScale(2) + "% do valor total)", col3, y + 20);
        
        y += 45;
        
        // ============ VALOR TOTAL DA NF ============
        g2.setColor(AZUL_BANCO);
        g2.fillRect(margin, y, width - 2*margin, 40);
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("VALOR TOTAL DA NOTA FISCAL:", col1, y + 27);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString(CURRENCY_FORMAT.format(nf.getValorTotalNf()), col4 - 50, y + 27);
        
        y += 55;
        
        // ============ INFORMAÇÕES ADICIONAIS ============
        g2.setColor(Color.GRAY);
        g2.setFont(new Font("Arial", Font.PLAIN, 8));
        g2.drawString("Documento emitido conforme normas da Receita Federal do Brasil e Secretaria da Fazenda Estadual.", margin, y);
        y += 10;
        g2.drawString("Tributos calculados conforme EC 132/2023 (Reforma Tributária) - IBS e CBS em vigor a partir de 2026.", margin, y);
        y += 10;
        g2.drawString("Consulte a autenticidade em: www.nfe.fazenda.gov.br - Chave de Acesso: " + (chave != null ? chave.substring(0, 20) + "..." : ""), margin, y);
        
        g2.dispose();
        return image;
    }
    
    /**
     * Cria um boleto a partir de uma transação
     */
    public Boleto criarBoletoDeTransacao(Transacao transacao, Usuario usuario, LocalDate vencimento) {
        Map<String, String> emissor = configDAO.getConfigEmissor();
        Map<String, String> banco = configDAO.getConfigBanco();
        
        Boleto boleto = new Boleto();
        
        // Dados bancários
        boleto.setCodigoBanco(banco.get("CODIGO"));
        boleto.setNomeBanco(banco.get("NOME"));
        boleto.setAgencia(banco.get("AGENCIA"));
        boleto.setConta(banco.get("CONTA"));
        boleto.setCarteira(banco.get("CARTEIRA"));
        boleto.setConvenio(banco.get("CONVENIO"));
        
        // Beneficiário (empresa)
        boleto.setBeneficiarioCpfCnpj(emissor.get("CNPJ"));
        boleto.setBeneficiarioNome(emissor.get("RAZAO_SOCIAL"));
        boleto.setBeneficiarioEndereco(emissor.get("ENDERECO"));
        boleto.setBeneficiarioCidadeUf(emissor.get("CIDADE") + "/" + emissor.get("UF"));
        
        // Pagador (usuário)
        boleto.setPagadorCpfCnpj(usuario.getEmail()); // Usar CPF se disponível
        boleto.setPagadorNome(usuario.getNome());
        boleto.setPagadorEndereco("Endereço do Cliente");
        boleto.setPagadorCidadeUf("Teresina/PI");
        boleto.setPagadorCep("64000-000");
        
        // Valores
        boleto.setValorDocumento(transacao.getValor().abs());
        boleto.setDataVencimento(vencimento);
        boleto.setIdUsuario(usuario.getId());
        boleto.setIdTransacao(transacao.getId());
        
        // Gerar nosso número
        boleto.gerarNossoNumero(transacao.getId());
        
        // Demonstrativo
        boleto.setDemonstrativo("Referente à transação #" + transacao.getId() + 
            " - " + transacao.getDescricao());
        
        // Gerar códigos
        boleto.gerarCodigoBarras();
        boleto.gerarLinhaDigitavel();
        
        return boleto;
    }
    
    /**
     * Cria uma NF a partir de uma transação
     */
    public NotaFiscal criarNotaFiscalDeTransacao(Transacao transacao, Usuario usuario) {
        Map<String, String> emissor = configDAO.getConfigEmissor();
        Map<String, BigDecimal> impostos = configDAO.getConfigImpostos();
        
        NotaFiscal nf = new NotaFiscal();
        
        // Número da NF
        int ultimoNumero = configDAO.getConfigInt("NF_ULTIMO_NUMERO");
        nf.setNumeroNf(String.format("%09d", ultimoNumero + 1));
        configDAO.saveConfig("NF_ULTIMO_NUMERO", String.valueOf(ultimoNumero + 1));
        
        // Emitente
        nf.setEmitenteCnpj(emissor.get("CNPJ"));
        nf.setEmitenteRazaoSocial(emissor.get("RAZAO_SOCIAL"));
        nf.setEmitenteNomeFantasia(emissor.get("NOME_FANTASIA"));
        nf.setEmitenteEndereco(emissor.get("ENDERECO"));
        nf.setEmitenteCidade(emissor.get("CIDADE"));
        nf.setEmitenteUf(emissor.get("UF"));
        nf.setEmitenteCep(emissor.get("CEP"));
        nf.setEmitenteIe(emissor.get("IE"));
        nf.setEmitenteIm(emissor.get("IM"));
        
        // Destinatário
        nf.setDestinatarioCpfCnpj("000.000.000-00"); // Usar CPF real se disponível
        nf.setDestinatarioNome(usuario.getNome());
        nf.setDestinatarioEmail(usuario.getEmail());
        nf.setDestinatarioCidade("Teresina");
        nf.setDestinatarioUf("PI");
        
        // Valores
        BigDecimal valor = transacao.getValor().abs();
        if (transacao.getTipo() != null && transacao.getTipo().equals("S")) {
            nf.setValorServicos(valor);
        } else {
            nf.setValorProdutos(valor);
        }
        
        // Calcular impostos
        BigDecimal aliqIbs = impostos.get("TAXA_IBS").divide(new BigDecimal("100"));
        BigDecimal aliqCbs = impostos.get("TAXA_CBS").divide(new BigDecimal("100"));
        BigDecimal aliqIs = impostos.get("TAXA_IS").divide(new BigDecimal("100"));
        
        nf.calcularImpostos(aliqIbs, aliqCbs, aliqIs);
        
        // Referências
        nf.setIdUsuario(usuario.getId());
        nf.setIdTransacao(transacao.getId());
        
        // Gerar chave de acesso
        nf.gerarChaveAcesso();
        
        return nf;
    }
    
    /**
     * Imprime um documento usando o sistema de impressão do Java
     */
    public void imprimirDocumento(BufferedImage imagem, String titulo) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName(titulo);
        
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            
            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            
            double scaleX = pageFormat.getImageableWidth() / imagem.getWidth();
            double scaleY = pageFormat.getImageableHeight() / imagem.getHeight();
            double scale = Math.min(scaleX, scaleY);
            
            g2.scale(scale, scale);
            g2.drawImage(imagem, 0, 0, null);
            
            return Printable.PAGE_EXISTS;
        });
        
        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(null, 
                    "Documento enviado para impressão!", 
                    "Impressão", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(null, 
                    "Erro ao imprimir: " + e.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Salva documento como imagem PNG
     */
    public void salvarComoImagem(BufferedImage imagem, String caminho) throws IOException {
        ImageIO.write(imagem, "PNG", new File(caminho));
    }
    
    /**
     * Mostra preview do documento
     */
    public void mostrarPreview(BufferedImage imagem, String titulo) {
        JFrame frame = new JFrame("Preview - " + titulo);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JLabel label = new JLabel(new ImageIcon(imagem));
        JScrollPane scrollPane = new JScrollPane(label);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton btnImprimir = new JButton("🖨️ Imprimir");
        btnImprimir.addActionListener(e -> imprimirDocumento(imagem, titulo));
        
        JButton btnSalvar = new JButton("💾 Salvar PNG");
        btnSalvar.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(titulo.replace(" ", "_") + ".png"));
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try {
                    salvarComoImagem(imagem, chooser.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(frame, "Arquivo salvo com sucesso!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Erro ao salvar: " + ex.getMessage());
                }
            }
        });
        
        JButton btnFechar = new JButton("❌ Fechar");
        btnFechar.addActionListener(e -> frame.dispose());
        
        buttonPanel.add(btnImprimir);
        buttonPanel.add(btnSalvar);
        buttonPanel.add(btnFechar);
        
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        
        frame.setSize(850, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
