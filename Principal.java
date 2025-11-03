import java.io.*;
import javax.swing.JFrame;

public class Principal {

	public static void main(String[] args)  throws Exception {
		telaLogin tela = new telaLogin();
   		tela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  		tela.setSize(250,400);
  		tela.setVisible(true);
	}

}