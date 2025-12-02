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
 
 public telaLogin() throws Exception {
  super("Cadastrar");
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
  
  lretorno = new JLabel("lretorno:");
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

        String key = "1234567890123456";
        String encryptedString = encrypt(tsenha.getText(), key);

         ClienteHTTP Conexao = new ClienteHTTP(tlogin.getText(),encryptedString,"http://www.datse.com.br/dev/syncjava.php");
	 String ret = Conexao.conecta();
	 tretorno.setText(ret);
         
         // Exibe no terminal a resposta da API
         System.out.println("============================================");
         System.out.println("   CONEXÃO COM API - RESPOSTA");
         System.out.println("============================================");
         System.out.println("[INFO] Usuário: " + tlogin.getText());
         System.out.println("[INFO] Código de retorno: " + Conexao.codretorno);
         System.out.println("[INFO] Resposta da API: " + ret);
         
         // Trata o JSON usando trataJSON
         if (ret != null && !ret.isEmpty()) {
           try {
             trataJSON tratador = new trataJSON(ret);
             pessoa resultado = tratador.tratarString();
             System.out.println("\n[OK] JSON processado com sucesso!");
             System.out.println("  - ID: " + resultado.getId());
             System.out.println("  - Modo: " + resultado.getModo());
             System.out.println("  - Valor: " + resultado.getValor());
             System.out.println("  - Mensagem: " + resultado.getRetmsg());
           } catch (Exception jsonEx) {
             System.out.println("[INFO] Resposta JSON (raw): " + ret);
           }
         }
         System.out.println("============================================\n");
         
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

}