import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

// Imports para integração com API PIX
import br.uespi.acessoapi.ClienteHTTP;
import br.uespi.acessoapi.PIXConexao;
import br.uespi.tratajson.trataJSON;
import br.uespi.tratajson.JSONObject;
import br.uespi.pessoas.pessoa;

public class telaLogin extends JFrame{
 private JTextField tlogin, tsenha, tretorno;
 private JButton logar, limpar;
 private JLabel llogin, lsenha, lretorno;

 private static final String ALGORITHM = "AES";
 private static final String API_URL = "http://www.datse.com.br/dev/syncjava.php";
 
 // Armazena credenciais para passar para próxima tela
 private String usuarioLogado = null;
 private String senhaLogado = null;
 
 public telaLogin() throws Exception {
  super("Login - Sistema PIX");
  setLayout(new FlowLayout());
  this.addComponentes();
 } 
 
 public void addComponentes() throws Exception  {
  llogin = new JLabel("Login:");
  add(llogin);
  
  tlogin = new JTextField(20);
  add(tlogin);
  
  lsenha = new JLabel("Senha:");
  add(lsenha);
  
  tsenha = new JTextField(20);
  add(tsenha);
  
  lretorno = new JLabel("Retorno:");
  add(lretorno);
  
  tretorno = new JTextField(20);
  add(tretorno);
  
  logar = new JButton("Logar");
  logarOnClick();
  
  limpar = new JButton("Limpar");
  limparOnClick();
 }

 public void logarOnClick()  {
  logar.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent evento) {
     if(evento.getSource() == logar) {
       try {
        String usuario = tlogin.getText();
        String senha = tsenha.getText();

         ClienteHTTP Conexao = new ClienteHTTP(usuario, senha, API_URL);
	 String ret = Conexao.conecta();
	 tretorno.setText(ret);
         
         // Exibe no terminal a resposta da API
         System.out.println("============================================");
         System.out.println("   CONEXÃO COM API - RESPOSTA");
         System.out.println("============================================");
         System.out.println("[INFO] Usuário: " + usuario);
         System.out.println("[INFO] Código de retorno: " + Conexao.codretorno);
         System.out.println("[INFO] Resposta da API: " + ret);
         
         // Verifica se login foi bem-sucedido
         boolean loginValido = verificarLogin(ret);
         
         if (loginValido) {
             usuarioLogado = usuario;
             senhaLogado = senha;
             // After successful API auth, fetch DB user to check roles and possibly show admin UI
             try {
                 controle.dao.UsuarioDAO udao = new controle.dao.UsuarioDAOImpl();
                 controle.model.Usuario dbUser = null;
                 if (usuario != null && usuario.contains("@")) {
                     dbUser = udao.findByEmail(usuario);
                 } else {
                     dbUser = udao.findByNome(usuario);
                 }
                 if (dbUser != null && dbUser.isAdmin()) {
                     System.out.println("[INFO] Usuário admin detectado: " + dbUser.getNome());
                     // Open main admin transaction UI
                     javax.swing.SwingUtilities.invokeLater(() -> {
                         controle.ui.TelaTransacao tela = new controle.ui.TelaTransacao(dbUser);
                         tela.setVisible(true);
                         telaLogin.this.setVisible(false);
                     });
                     return;
                 }
             } catch (Exception e) {
                 System.err.println("[WARN] Não foi possível verificar papel do usuário no DB: " + e.getMessage());
             }
             
             System.out.println("\n[OK] LOGIN AUTORIZADO!");
             System.out.println("[INFO] Abrindo tela de pagamento PIX...");
             System.out.println("============================================\n");
             
             // Abre a tela de pagamento PIX
             abrirTelaPagamento();
         } else {
             System.out.println("\n[ERRO] LOGIN NEGADO!");
             System.out.println("[INFO] Verifique suas credenciais.");
             System.out.println("============================================\n");
         }
         
       } catch (Exception e) {
         System.err.println("[ERRO] Falha na conexão: " + e.getMessage());
         tretorno.setText("Erro: " + e.getMessage());
       }   
     }
    }
   }
  );
  add(logar);
 }
 
 /**
  * Verifica se a resposta indica login válido
  */
 private boolean verificarLogin(String resposta) {
     if (resposta == null || resposta.isEmpty()) {
         return false;
     }
     String respostaLower = resposta.toLowerCase();
     // Verifica mensagens de sucesso
     if (respostaLower.contains("sucesso") || respostaLower.contains("realizado")) {
         return true;
     }
     // Verifica mensagens de erro
     if (respostaLower.contains("invalido") || respostaLower.contains("erro") || respostaLower.contains("negado")) {
         return false;
     }
     return false;
 }
 
 /**
  * Abre a tela de pagamento PIX após login bem-sucedido
  */
 private void abrirTelaPagamento() {
     this.setVisible(false);
     TelaPagamentoPIXAuth telaPagamento = new TelaPagamentoPIXAuth(usuarioLogado, senhaLogado, this);
     telaPagamento.setVisible(true);
 }
 
 /**
  * Método para reexibir a tela de login (chamado ao fazer logout)
  */
 public void mostrarLogin() {
     usuarioLogado = null;
     senhaLogado = null;
     tlogin.setText("");
     tsenha.setText("");
     tretorno.setText("");
     this.setVisible(true);
 }
 
 public void limparOnClick() {
  limpar.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent evento) {
    if(evento.getSource() == limpar){
      tlogin.setText("");
      tsenha.setText("");
      tretorno.setText("");
     }

    }
   }
  );
  add(limpar);
 }

 public static String encrypt(String data, String key) throws Exception {
    SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    byte[] encryptedData = cipher.doFinal(data.getBytes());
    return Base64.getEncoder().encodeToString(encryptedData);
 }
 
 public static void main(String[] args) {
     try {
         telaLogin tela = new telaLogin();
         tela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         tela.setSize(280, 200);
         tela.setLocationRelativeTo(null);
         tela.setVisible(true);
         
         System.out.println("============================================");
         System.out.println("   SISTEMA DE PAGAMENTO PIX");
         System.out.println("============================================");
         System.out.println("[INFO] Tela de login iniciada");
         System.out.println("[INFO] API URL: http://www.datse.com.br/dev/syncjava.php");
         System.out.println("[INFO] Aguardando autenticação...");
         System.out.println("============================================\n");
     } catch (Exception e) {
         System.err.println("Erro ao iniciar: " + e.getMessage());
     }
 }

}