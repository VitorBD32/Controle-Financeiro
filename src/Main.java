package controle;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import controle.ui.TelaLoginPrincipal;

/**
 * Ponto de entrada principal do Sistema de Controle Financeiro Premium
 * Inicia com a tela de login para autenticação segura
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("💰 SISTEMA DE CONTROLE FINANCEIRO PREMIUM 💰");
        System.out.println("=".repeat(60));
        System.out.println("🔐 Iniciando sistema com autenticação segura...");
        System.out.println("📊 Recursos: Pagamentos PIX, Cartões Criptografados, QR Code");
        System.out.println("-".repeat(60));
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Define Look and Feel nativo do sistema
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Inicia a tela de login principal
                TelaLoginPrincipal telaLogin = new TelaLoginPrincipal();
                telaLogin.setVisible(true);
                
                System.out.println("✅ Interface gráfica iniciada com sucesso!");
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro ao iniciar interface gráfica", e);
                System.err.println("❌ Erro ao iniciar sistema: " + e.getMessage());
            }
        });
    }
}
