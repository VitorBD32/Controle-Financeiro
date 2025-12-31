package controle.examples;

import controle.api.TributosAPIClient;
import controle.model.TributosCalculados;
import controle.model.NotaFiscal;
import controle.util.DocumentoFiscalGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Exemplo de uso da API de cálculo de tributos integrada com Nota Fiscal
 * 
 * Demonstra como:
 * 1. Calcular tributos (CBS, IBS, IS) via API
 * 2. Preencher NotaFiscal com valores calculados
 * 3. Gerar comprovante/NF com tributos exibidos
 * 
 * @author Sistema de Controle Financeiro
 * @version 1.0
 */
public class ExemploTributosAPI {
    
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  EXEMPLO: CÁLCULO DE TRIBUTOS VIA API");
        System.out.println("==============================================\n");
        
        // 1. Criar cliente da API de tributos
        TributosAPIClient apiClient = new TributosAPIClient();
        
        // 2. Dados da transação
        BigDecimal valorBase = new BigDecimal("1000.00");  // R$ 1.000,00
        String uf = "PI";                                  // Piauí
        String municipio = "Teresina";                     // Capital
        String tipoServico = "Serviço de Consultoria";    // Não sujeito a IS
        
        System.out.println("📊 Calculando tributos para:");
        System.out.println("   Valor Base: R$ " + valorBase);
        System.out.println("   UF: " + uf);
        System.out.println("   Município: " + municipio);
        System.out.println("   Tipo: " + tipoServico);
        System.out.println();
        
        // 3. Calcular tributos via API
        TributosCalculados tributos = apiClient.calcularImpostos(valorBase, uf, municipio, tipoServico);
        
        // 4. Exibir resultado
        System.out.println("✅ TRIBUTOS CALCULADOS:");
        System.out.println("   CBS (Federal): R$ " + tributos.getValorCBS() + 
                          " (" + tributos.getAliquotaCBS().multiply(new BigDecimal("100")) + "%)");
        System.out.println("   IBS (Estadual/Municipal): R$ " + tributos.getValorIBS() + 
                          " (" + tributos.getAliquotaIBS().multiply(new BigDecimal("100")) + "%)");
        System.out.println("   IS (Seletivo): R$ " + tributos.getValorIS() + 
                          " (" + tributos.getAliquotaIS().multiply(new BigDecimal("100")) + "%)");
        System.out.println("   ----------------------------------------");
        System.out.println("   TOTAL TRIBUTOS: R$ " + tributos.getTotalTributos());
        System.out.println("   Percentual sobre base: " + tributos.getPercentualTotal() + "%");
        System.out.println("   Valor Líquido: R$ " + tributos.getValorLiquido());
        System.out.println();
        
        // 5. Criar Nota Fiscal e preencher com tributos calculados
        NotaFiscal nf = criarNotaFiscalExemplo(valorBase, tributos);
        
        System.out.println("📄 NOTA FISCAL CRIADA:");
        System.out.println("   Número: " + nf.getNumeroNf());
        System.out.println("   Valor Produtos: R$ " + nf.getValorProdutos());
        System.out.println("   CBS: R$ " + nf.getValorCBS());
        System.out.println("   IBS: R$ " + nf.getValorIBS());
        System.out.println("   IS: R$ " + nf.getValorIS());
        System.out.println("   Total NF: R$ " + nf.getValorTotalNf());
        System.out.println();
        
        // 6. Gerar comprovante (DocumentoFiscalGenerator exibe tributos automaticamente)
        System.out.println("🖨️  Gerando comprovante fiscal com tributos...");
        DocumentoFiscalGenerator generator = new DocumentoFiscalGenerator();
        try {
            // Este método gera imagem da NF com seção de tributos
            var imagemNF = generator.gerarImagemNotaFiscal(nf);
            System.out.println("✅ Comprovante gerado com sucesso!");
            System.out.println("   Dimensões: " + imagemNF.getWidth() + "x" + imagemNF.getHeight());
            System.out.println("   Tributos exibidos no documento: CBS, IBS, IS");
        } catch (Exception e) {
            System.err.println("❌ Erro ao gerar comprovante: " + e.getMessage());
        }
        
        System.out.println("\n==============================================");
        System.out.println("  INTEGRAÇÃO CONCLUÍDA COM SUCESSO!");
        System.out.println("==============================================");
    }
    
    /**
     * Cria uma Nota Fiscal de exemplo e preenche com tributos calculados
     */
    private static NotaFiscal criarNotaFiscalExemplo(BigDecimal valorBase, TributosCalculados tributos) {
        NotaFiscal nf = new NotaFiscal();
        
        // Dados básicos
        nf.setNumeroNf("NF-" + System.currentTimeMillis());
        nf.setSerie("001");
        nf.setTipoNf(NotaFiscal.TipoNF.NFE);
        nf.setDataEmissao(LocalDateTime.now());
        
        // Emitente
        nf.setEmitenteCnpj("12.345.678/0001-99");
        nf.setEmitenteRazaoSocial("CONTROLE FINANCEIRO LTDA");
        nf.setEmitenteNomeFantasia("Controle Financeiro Premium");
        nf.setEmitenteEndereco("Rua das Empresas, 123");
        nf.setEmitenteCidade("Teresina");
        nf.setEmitenteUf("PI");
        nf.setEmitenteCep("64000-000");
        nf.setEmitenteIe("123456789");
        nf.setEmitenteIm("9876543");
        
        // Localização para cálculo de tributos
        nf.setUfEmitente(tributos.getUf());
        nf.setMunicipioEmitente(tributos.getMunicipio());
        
        // Destinatário
        nf.setDestinatarioCpfCnpj("123.456.789-00");
        nf.setDestinatarioNome("Cliente Exemplo");
        nf.setDestinatarioEndereco("Av. Principal, 456");
        nf.setDestinatarioCidade("Teresina");
        nf.setDestinatarioUf("PI");
        nf.setDestinatarioCep("64001-000");
        
        // Valores
        nf.setValorProdutos(valorBase);
        nf.setValorServicos(BigDecimal.ZERO);
        nf.setValorDesconto(BigDecimal.ZERO);
        nf.setValorFrete(BigDecimal.ZERO);
        
        // Tributos calculados via API
        nf.setValorCBS(tributos.getValorCBS());
        nf.setAliquotaCbs(tributos.getAliquotaCBS());
        
        nf.setValorIBS(tributos.getValorIBS());
        nf.setAliquotaIbs(tributos.getAliquotaIBS());
        
        nf.setValorIS(tributos.getValorIS());
        nf.setAliquotaIs(tributos.getAliquotaIS());
        
        // Total
        BigDecimal totalTributos = tributos.getTotalTributos();
        nf.setValorTotalImpostos(totalTributos);
        nf.setValorTotalNf(valorBase.add(totalTributos));
        
        nf.setStatus(NotaFiscal.StatusNF.EMITIDA);
        
        return nf;
    }
}
