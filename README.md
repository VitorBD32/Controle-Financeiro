Projeto: Controle Financeiro (esqueleto)

Estrutura criada:
- avaliacao1/schema.sql -> script para criar banco e tabelas
- src/Conexao.java -> utilitários de conexão JDBC
- src/model/Usuario.java, Categoria.java, Transacao.java -> modelos simples
- src/dao/UsuarioDAO.java, UsuarioDAOImpl.java -> exemplo de DAO com CRUD
- src/Main.java -> exemplo de uso (listar/checar conexão)

Como usar:
1) Execute o script `avaliacao1/schema.sql` no MySQL Workbench (ou mysql CLI).
2) Atualize as credenciais em `src/Conexao.java` (URL, USER, PASSWORD).
3) Compile: javac -d out src\*.java src\model\*.java src\dao\*.java
4) Execute: java -cp out Main

Posso adaptar para Maven/Gradle se preferir.
