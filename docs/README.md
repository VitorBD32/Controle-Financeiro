# Documentação do Projeto: Controle Financeiro

## 1. Visão Geral

Este é um sistema de desktop para controle financeiro pessoal, desenvolvido em Java. Ele permite que os usuários gerenciem suas transações financeiras (receitas e despesas), categorizem-nas e visualizem seu saldo. O projeto utiliza uma arquitetura simples com separação de responsabilidades, incluindo modelos de dados, DAOs (Data Access Objects) para interação com o banco de dados e telas de interface de usuário (UI).

## 2. Tecnologias Utilizadas

- **Linguagem:** Java 11
- **Gerenciador de Build e Dependências:** Apache Maven
- **Banco de Dados:** MySQL (conectividade via `mysql-connector-j`)
- **UI:** Java Swing (para a interface gráfica de usuário)
- **Segurança:** jBCrypt (para hashing de senhas de usuário)

## 3. Estrutura do Projeto

O projeto é organizado da seguinte forma:

```
controle-financeiro/
├── pom.xml                # Arquivo de configuração do Maven
├── config/
│   └── db.properties      # Configurações de acesso ao banco de dados
├── avaliacao1/
│   └── schema.sql         # Script SQL para criação do esquema do banco
├── src/main/java/controle/
│   ├── config/            # Classes de configuração (ex: DBConfig.java)
│   ├── dao/               # DAOs: Classes de acesso aos dados (ex: UsuarioDAO)
│   ├── model/             # Modelos de dados (ex: Usuario, Transacao)
│   ├── ui/                # Telas da interface gráfica (ex: TelaUsuario)
│   └── Main.java          # Ponto de entrada principal (para testes via CLI)
├── build.ps1              # Script para compilar o projeto
└── run-ui.ps1             # Script para executar a interface gráfica
```

- **`pom.xml`**: Define as dependências do projeto (MySQL Connector, jBCrypt) e os plugins do Maven, como o `maven-compiler-plugin` e o `exec-maven-plugin`.
- **`src/main/java/controle`**: Pacote principal do código-fonte.
  - **`model`**: Contém as classes de entidade (POJOs) que representam os dados da aplicação, como `Usuario`, `Categoria` e `Transacao`.
  - **`dao`**: Implementa o padrão Data Access Object. Isola a lógica de acesso ao banco de dados do resto da aplicação.
  - **`config`**: Centraliza a configuração do banco de dados, lendo as propriedades do arquivo `config/db.properties`.
  - **`ui`**: Contém as classes que constroem a interface gráfica do usuário utilizando a biblioteca Swing.
  - **`Main.java`**: Uma classe simples para testar a conexão com o banco de dados e listar usuários diretamente no console.

### Aprofundando: O Papel do Maven

O **Apache Maven** é uma ferramenta fundamental para este projeto, atuando em duas frentes principais: **gerenciamento de dependências** e **automação de build**.

1.  **Gerenciamento de Dependências**:
    - Antes do Maven, era comum baixar arquivos JAR (bibliotecas) manualmente e adicioná-los a uma pasta `lib/` no projeto. Isso era propenso a erros, difícil de atualizar e de compartilhar com outros desenvolvedores.
    - O Maven resolve isso através do arquivo `pom.xml`. Nele, declaramos as bibliotecas que nosso projeto necessita (como o conector do MySQL e o jBCrypt) na seção `<dependencies>`.
    - Ao compilar o projeto, o Maven lê essas declarações, baixa automaticamente as versões corretas das bibliotecas de um repositório central (o Maven Central Repository) e as disponibiliza para o nosso projeto. Isso garante que todos os desenvolvedores usem as mesmas versões e simplifica drasticamente a configuração do ambiente.

2.  **Automação de Build (Ciclo de Vida)**:
    - O Maven define um ciclo de vida padrão para a construção de um projeto, com fases como `validate`, `compile`, `test`, `package`, `install` e `deploy`.
    - Ao executar um comando como `mvn clean install`, o Maven executa uma série de tarefas em ordem:
        - `clean`: Apaga o diretório `target/`, que contém os artefatos da compilação anterior.
        - `compile`: Compila o código-fonte Java (`.java`) para bytecode (`.class`).
        - `package`: Empacota o código compilado em um formato distribuível, que no nosso caso é um arquivo JAR.
        - `install`: Instala o pacote no seu repositório Maven local, tornando-o disponível para outros projetos na sua máquina.
    - Essa automação garante que o processo de "construir" o software seja padronizado, repetível e menos suscetível a erros humanos.

Em resumo, o Maven padroniza e automatiza a forma como o projeto é construído e como suas dependências externas são gerenciadas, tornando o desenvolvimento mais eficiente e colaborativo.

## 4. Banco de Dados

O sistema utiliza um banco de dados MySQL para persistir os dados. O script `avaliacao1/schema.sql` contém as instruções `CREATE TABLE` para as tabelas principais: `usuarios`, `categorias` e `transacoes`.

**Observação:** O script `schema.sql` no projeto está marcado como um exemplo histórico. O banco de dados real deve ser criado e gerenciado no ambiente MySQL do desenvolvedor (ex: MySQL Workbench).

## 5. Como Compilar e Executar

### Pré-requisitos

1.  **JDK 11** (ou superior) instalado e configurado.
2.  **Apache Maven** instalado e configurado.
3.  **MySQL Server** instalado e em execução.

### Passos

1.  **Configurar o Banco de Dados:**
    - Crie um banco de dados no seu servidor MySQL (ex: `CREATE DATABASE controle_financeiro;`).
    - Execute as queries do arquivo `avaliacao1/schema.sql` para criar as tabelas.
    - Edite o arquivo `config/db.properties` com as suas credenciais de acesso ao banco de dados (URL, usuário e senha).

2.  **Compilar o Projeto:**
    Você pode usar o script PowerShell fornecido ou o Maven diretamente.
    - **Via script:**
      ```powershell
      .\build.ps1
      ```
    - **Via Maven:**
      ```bash
      mvn clean install
      ```
      Este comando irá compilar o código-fonte e empacotá-lo em um arquivo JAR no diretório `target`.

3.  **Executar a Aplicação (Interface Gráfica):**
    O script `run-ui.ps1` foi criado para facilitar a execução da interface gráfica.
    ```powershell
    .\run-ui.ps1
    ```
    Este script utiliza o Maven para construir o classpath das dependências e, em seguida, executa a tela principal `controle.ui.TelaUsuario`.

4.  **Executar Teste de Conexão (Linha de Comando):**
    Para um teste rápido da conexão com o banco de dados, você pode executar a classe `Main` via Maven:
    ```bash
    mvn exec:java
    ```
Comando para rodar projeto
.\run-ui.ps1

## 6. Integração com API externa

O projeto pode ser integrado com uma API externa para sincronização de dados. A URL alvo desta integração (produção) é:

```
http://www.datse.com.br/dev/syncjava2.php
```

Para testes locais, use o mock server incluído no repositório em `http://127.0.0.1:8000/syncjava.php`.

Abaixo está um exemplo mínimo em Java que demonstra como o aplicativo desktop pode enviar uma requisição HTTP POST para essa URL com parâmetros codificados (UTF-8). Você pode adaptar este código para enviar dados do banco de dados (por exemplo, usuários, transações ou categorias) ao endpoint.

Exemplo de snippet Java:

```java
public static void main(String[] args) throws Exception {
    String url = "http://www.datse.com.br/dev/syncjava2.php"; // para mock local, use "http://127.0.0.1:8000/syncjava.php"
    String urlParameters = "name=" + URLEncoder.encode("UESPI", "UTF-8") + "&age="
            + URLEncoder.encode("2025", "UTF-8");

    // Exemplo simplificado: abrir conexão, enviar POST e ler resposta
    java.net.URL obj = new java.net.URL(url);
    java.net.HttpURLConnection con = (java.net.HttpURLConnection) obj.openConnection();
    con.setRequestMethod("POST");
    con.setDoOutput(true);
    try (java.io.DataOutputStream wr = new java.io.DataOutputStream(con.getOutputStream())) {
        wr.writeBytes(urlParameters);
        wr.flush();
    }

    int responseCode = con.getResponseCode();
    System.out.println("Response Code : " + responseCode);
    try (java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(con.getInputStream()))) {
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        System.out.println("Response: " + response.toString());
    }
}
```

Recomendações de integração:

- Proteja as credenciais — se for necessário autenticar na API, armazene chaves/segredos fora do código (ex: `config/db.properties` ou variáveis de ambiente) e não as versionar.
- Envie apenas os dados necessários e trate erros de rede (timeouts, respostas não-200).
- Considere usar bibliotecas HTTP (Apache HttpClient, OkHttp) para requisições mais robustas.
- Se for sincronizar dados do banco local, implemente marcação de estado (por exemplo, campo `synced BOOLEAN` ou `last_synced TIMESTAMP`) para evitar duplicação.

Nota: o snippet acima é um exemplo básico; adapte-o ao modelo de dados e ao fluxo de negócio do seu projeto `controle-financeiro`.

## 7. Segurança: criptografia simétrica (AES)

Para proteger dados sensíveis durante a sincronização com a API (por exemplo, informações pessoais ou credenciais), recomendamos usar criptografia simétrica AES. Abaixo estão orientações e um exemplo em Java para geração de chave derivada de senha (PBKDF2), encriptação e decriptação usando AES/CBC/PKCS5Padding.

Pontos importantes:

- Use AES com chave de 128 ou 256 bits (dependendo da política de JCE do Java). Para 256 bits, o JDK deve permitir chaves de tamanho maior (nas versões recentes do JDK isso já vem habilitado).
- Nunca reutilize IVs (vetores de inicialização) — gere IVs aleatórios por operação e armazene/encaminhe o IV junto com o ciphertext.
- Use PBKDF2 (Password-Based Key Derivation Function 2) para derivar uma chave segura a partir de uma senha/segredo, com salt e iterações suficientes (ex.: 10000+).
- Proteja a senha/segredo que deriva a chave; armazene-o de forma segura (variáveis de ambiente, cofre de segredos, etc.).

Exemplo em Java (derivação PBKDF2 + AES/CBC/PKCS5Padding):

```java
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AESUtil {
    private static final String ALGO = "AES/CBC/PKCS5Padding";
    private static final String KDF_ALGO = "PBKDF2WithHmacSHA256";

    public static SecretKeySpec deriveKey(char[] password, byte[] salt, int iterations, int keyLen) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLen);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(KDF_ALGO);
        SecretKey key = skf.generateSecret(spec);
        return new SecretKeySpec(key.getEncoded(), "AES");
    }

    public static String encrypt(String plainText, SecretKeySpec key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGO);
        byte[] iv = new byte[16];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String cipherTextBase64, SecretKeySpec key) throws Exception {
        byte[] combined = Base64.getDecoder().decode(cipherTextBase64);
        byte[] iv = new byte[16];
        byte[] encrypted = new byte[combined.length - 16];
        System.arraycopy(combined, 0, iv, 0, 16);
        System.arraycopy(combined, 16, encrypted, 0, encrypted.length);
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] original = cipher.doFinal(encrypted);
        return new String(original, "UTF-8");
    }

    // Exemplo de uso
    public static void main(String[] args) throws Exception {
        char[] password = "sua-senha-secreta".toCharArray(); // ideal: obter de variável de ambiente
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        SecretKeySpec key = deriveKey(password, salt, 20000, 256);

        String plain = "dados-sensiveis-a-enviar";
        String cipherText = encrypt(plain, key);
        System.out.println("Cipher (base64): " + cipherText);

        String decrypted = decrypt(cipherText, key);
        System.out.println("Decrypted: " + decrypted);
    }
}
```

Recomendações práticas:

- Armazene o salt e o IV associados ao ciphertext (por exemplo, prefixados ao ciphertext como no exemplo acima) para permitir decriptação.
- Para autenticação de integridade e proteção contra adulteração, considere usar AES-GCM (Authenticated Encryption) ou calcular um HMAC separado sobre o ciphertext.
- Não exponha a senha ou chave em repositórios. Prefira variáveis de ambiente ou serviços de cofre.
- Teste performance: derivação PBKDF2 com muitas iterações aumenta segurança mas consome CPU; ajuste conforme necessidade.

Implementação no `controle-financeiro`:

- Crie uma classe utilitária (por exemplo `controle.util.AESUtil`) com métodos para derivar chave, encriptar e decriptar.
- Ao enviar dados para o endpoint de sincronização (ex.: `http://www.datse.com.br/dev/syncjava2.php` ou, para testes locais, `http://127.0.0.1:8000/syncjava.php`), você pode antes cifrar o payload JSON e enviar o conteúdo cifrado (base64) e o salt/iv necessários para decriptação no servidor.
- Documente no `config/` onde configurar a senha/segredo para derivação (ex.: `config/api.properties` ou variável de ambiente `API_SYNC_SECRET`).

Com isso, finalize a etapa de documentação da segurança; se quiser, posso também:

- Implementar `AESUtil` em `src/main/java/controle/util/AESUtil.java` e escrever testes básicos.
- Adicionar suporte de configuração em `config/api.properties` e leitura em `controle.config.DBConfig` (ou criar `APIConfig`).