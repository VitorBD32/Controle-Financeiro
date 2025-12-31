package controle.examples;

import java.math.BigDecimal;

import javax.swing.SwingUtilities;

import controle.api.TributosAPIClient;
import controle.model.Localizacao;
import controle.model.TributosCalculados;
import controle.ui.DialogoLocalizacao;

/**
 * Exemplo de uso da detecção automática de localização
 * para cálculo de tributos baseado em UF/Município
 * 
 * Demonstra:
 * 1. Solicitação de permissão ao usuário
 * 2. Detecção de localização via IP
 * 3. Cálculo automático de tributos (CBS, IBS, IS)
 * 
 * @author Sistema de Controle Financeiro
 * @version 1.0
 */
public class ExemploGeolocalizacaoTributos {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            executarExemplo();
        });
    }
    
    private static void executarExemplo() {
        System.out.println("========================================");
        System.out.println("  EXEMPLO: GEOLOCALIZAÇÃO + TRIBUTOS");
        System.out.println("========================================\n");
        
        // 1. Solicitar permissão para detectar localização
        System.out.println("📍 Solicitando permissão para detectar localização...\n");
        
        Localizacao localizacao = DialogoLocalizacao.solicitarLocalizacao(null);
        
        if (localizacao != null) {
            System.out.println("✅ LOCALIZAÇÃO DETECTADA:");
            System.out.println("   Cidade: " + localizacao.getCidade());
            System.out.println("   Estado: " + localizacao.getRegiao() + " (" + localizacao.getUf() + ")");
            System.out.println("   Coordenadas: " + localizacao.getLatitude() + ", " + localizacao.getLongitude());
            System.out.println("   Fonte: " + localizacao.getFonte());
            System.out.println();
            
            // 2. Calcular tributos com base na localização detectada
            BigDecimal valorTransacao = new BigDecimal("1500.00");
            System.out.println("💰 Calculando tributos para R$ " + valorTransacao);
            System.out.println("   Baseado em: " + localizacao.getDescricaoResumida());
            System.out.println();
            
            TributosAPIClient tributosClient = new TributosAPIClient();
            TributosCalculados tributos = tributosClient.calcularImpostos(
                valorTransacao,
                localizacao.getUf(),
                localizacao.getCidade(),
                "Serviço de Consultoria"
            );
            
            // 3. Exibir resultado
            System.out.println("📊 TRIBUTOS CALCULADOS:");
            System.out.println("   ┌─────────────────────────────────────────┐");
            System.out.println("   │ CBS (Federal): R$ " + String.format("%8.2f", tributos.getValorCBS()) + 
                             " (" + tributos.getAliquotaCBS().multiply(new BigDecimal("100")) + "%)");
            System.out.println("   │ IBS (Estadual/Municipal): R$ " + String.format("%8.2f", tributos.getValorIBS()) + 
                             " (" + tributos.getAliquotaIBS().multiply(new BigDecimal("100")) + "%)");
            System.out.println("   │ IS (Seletivo): R$ " + String.format("%8.2f", tributos.getValorIS()) + 
                             " (" + tributos.getAliquotaIS().multiply(new BigDecimal("100")) + "%)");
            System.out.println("   ├─────────────────────────────────────────┤");
            System.out.println("   │ TOTAL TRIBUTOS: R$ " + String.format("%8.2f", tributos.getTotalTributos()));
            System.out.println("   │ Percentual: " + tributos.getPercentualTotal() + "%");
            System.out.println("   │ Valor Líquido: R$ " + String.format("%8.2f", tributos.getValorLiquido()));
            System.out.println("   └─────────────────────────────────────────┘");
            System.out.println();
            
            // 4. Exemplo de cálculo automático (sem solicitar localização)
            System.out.println("🔄 CÁLCULO AUTOMÁTICO (sem diálogo):");
            TributosCalculados tributosAuto = tributosClient.calcularImpostosComLocalizacaoAutomatica(
                valorTransacao,
                "Serviço"
            );
            System.out.println("   Total Tributos: R$ " + tributosAuto.getTotalTributos());
            System.out.println("   Localização: " + tributosAuto.getUf() + " / " + tributosAuto.getMunicipio());
            
        } else {
            System.out.println("❌ Usuário negou permissão para detectar localização");
            System.out.println("   Usando alíquotas padrão nacionais...\n");
            
            // Fallback: calcular com alíquotas padrão
            BigDecimal valorTransacao = new BigDecimal("1500.00");
            TributosAPIClient tributosClient = new TributosAPIClient();
            TributosCalculados tributos = tributosClient.calcularImpostosComLocalizacaoAutomatica(
                valorTransacao,
                "Serviço"
            );
            
            System.out.println("📊 TRIBUTOS (Alíquotas Padrão):");
            System.out.println("   Total: R$ " + tributos.getTotalTributos());
        }
        
        System.out.println("\n========================================");
        System.out.println("  EXEMPLO CONCLUÍDO");
        System.out.println("========================================");
        
        System.exit(0);
    }
}
