package controle.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Gerador de QR Code PIX usando ZXing
 * Gera QR Codes dinâmicos para pagamentos PIX
 */
public class QRCodePIXGenerator {

    // Dados do recebedor PIX (configurar conforme necessário)
    private static final String CHAVE_PIX_PADRAO = "vitordebrito23@gmail.com"; // Chave PIX padrão
    private static final String NOME_RECEBEDOR = "CONTROLE FINANCEIRO";
    private static final String CIDADE_RECEBEDOR = "TERESINA";

    /**
     * Gera uma imagem de QR Code PIX
     * @param valor Valor do pagamento
     * @param descricao Descrição do pagamento
     * @param chavePix Chave PIX do recebedor (ou usa padrão)
     * @param tamanho Tamanho da imagem em pixels
     * @return BufferedImage do QR Code
     */
    public static BufferedImage gerarQRCodePIX(BigDecimal valor, String descricao, String chavePix, int tamanho) {
        try {
            // Usa chave padrão se não informada
            if (chavePix == null || chavePix.trim().isEmpty()) {
                chavePix = CHAVE_PIX_PADRAO;
            }
            
            // Gera o payload PIX (código EMV)
            String payloadPIX = gerarPayloadPIX(chavePix, valor, descricao);
            
            System.out.println("📱 Gerando QR Code PIX...");
            System.out.println("   Chave: " + chavePix);
            System.out.println("   Valor: R$ " + valor);
            System.out.println("   Payload: " + payloadPIX);
            
            // Gera o QR Code usando ZXing
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 2);
            
            BitMatrix bitMatrix = qrCodeWriter.encode(payloadPIX, BarcodeFormat.QR_CODE, tamanho, tamanho, hints);
            
            // Converte para BufferedImage
            BufferedImage image = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, tamanho, tamanho);
            graphics.setColor(Color.BLACK);
            
            for (int x = 0; x < tamanho; x++) {
                for (int y = 0; y < tamanho; y++) {
                    if (bitMatrix.get(x, y)) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }
            
            graphics.dispose();
            
            System.out.println("✅ QR Code PIX gerado com sucesso!");
            return image;
            
        } catch (WriterException e) {
            System.err.println("❌ Erro ao gerar QR Code: " + e.getMessage());
            return gerarImagemErro(tamanho, "Erro ao gerar QR Code");
        }
    }

    /**
     * Gera o payload PIX no formato EMV (simplificado)
     * Baseado na especificação do Banco Central do Brasil
     */
    private static String gerarPayloadPIX(String chavePix, BigDecimal valor, String descricao) {
        StringBuilder payload = new StringBuilder();
        
        // Payload Format Indicator (ID 00)
        payload.append("000201");
        
        // Merchant Account Information - PIX (ID 26)
        StringBuilder merchantAccount = new StringBuilder();
        // GUI (ID 00) - br.gov.bcb.pix
        merchantAccount.append("0014br.gov.bcb.pix");
        // Chave PIX (ID 01)
        merchantAccount.append("01").append(String.format("%02d", chavePix.length())).append(chavePix);
        
        // Se tem descrição, adiciona (ID 02)
        if (descricao != null && !descricao.trim().isEmpty() && descricao.length() <= 25) {
            merchantAccount.append("02").append(String.format("%02d", descricao.length())).append(descricao);
        }
        
        payload.append("26").append(String.format("%02d", merchantAccount.length())).append(merchantAccount);
        
        // Merchant Category Code (ID 52)
        payload.append("52040000");
        
        // Transaction Currency (ID 53) - BRL = 986
        payload.append("5303986");
        
        // Transaction Amount (ID 54) - se valor > 0
        if (valor != null && valor.compareTo(BigDecimal.ZERO) > 0) {
            String valorStr = valor.setScale(2).toPlainString();
            payload.append("54").append(String.format("%02d", valorStr.length())).append(valorStr);
        }
        
        // Country Code (ID 58)
        payload.append("5802BR");
        
        // Merchant Name (ID 59)
        String nome = NOME_RECEBEDOR.length() > 25 ? NOME_RECEBEDOR.substring(0, 25) : NOME_RECEBEDOR;
        payload.append("59").append(String.format("%02d", nome.length())).append(nome);
        
        // Merchant City (ID 60)
        String cidade = CIDADE_RECEBEDOR.length() > 15 ? CIDADE_RECEBEDOR.substring(0, 15) : CIDADE_RECEBEDOR;
        payload.append("60").append(String.format("%02d", cidade.length())).append(cidade);
        
        // Additional Data Field (ID 62) - txid
        String txid = "***"; // txid dinâmico
        String additionalData = "05" + String.format("%02d", txid.length()) + txid;
        payload.append("62").append(String.format("%02d", additionalData.length())).append(additionalData);
        
        // CRC16 (ID 63) - placeholder, será calculado
        payload.append("6304");
        
        // Calcula CRC16
        String crc = calcularCRC16(payload.toString());
        payload.append(crc);
        
        return payload.toString().replace("6304" + crc, "6304" + crc);
    }

    /**
     * Calcula o CRC16 do payload PIX
     * Polinômio: 0x1021 (CCITT)
     */
    private static String calcularCRC16(String payload) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;
        
        byte[] bytes = payload.getBytes();
        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i)) & 1) == 1;
                boolean c15 = ((crc >> 15) & 1) == 1;
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }
        
        crc &= 0xFFFF;
        return String.format("%04X", crc);
    }

    /**
     * Gera uma imagem de erro quando não consegue criar o QR Code
     */
    private static BufferedImage gerarImagemErro(int tamanho, String mensagem) {
        BufferedImage image = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, tamanho, tamanho);
        g.setColor(Color.RED);
        g.drawString(mensagem, 10, tamanho / 2);
        g.dispose();
        return image;
    }

    /**
     * Gera QR Code com valores padrão
     */
    public static BufferedImage gerarQRCodePIX(BigDecimal valor, String descricao) {
        return gerarQRCodePIX(valor, descricao, CHAVE_PIX_PADRAO, 200);
    }

    /**
     * Gera QR Code apenas com a chave (sem valor fixo)
     */
    public static BufferedImage gerarQRCodePIXSemValor(String chavePix) {
        return gerarQRCodePIX(null, null, chavePix, 200);
    }
}
