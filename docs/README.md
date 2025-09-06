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