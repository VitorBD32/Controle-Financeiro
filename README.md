Projeto: Controle Financeiro (Esqueleto e Demo)
===============================================

Resumo
------
Projeto em Java (OpenJDK 17) — interface desktop Swing — que fornece um sistema de controle financeiro com geração de relatórios, emissão de documentos fiscais (nota fiscal e boleto), integração por API, e conectividade com banco de dados MySQL (PROVA1).

Atualizações recentes (Resumo)
------------------------------
- Interface de usuário remodelada com foco em usabilidade e Material Design (temas, cores, botões e cards para KPI). ✅
- Dashboard com gráficos modernos (barras, área, linhas e donut) e cards KPI para métricas principais. ✅
- Integração com a reforma tributária 2026 (EC 132/2023): modelo de impostos (CBS, IBS, IS) e cálculo integrado para notas fiscais/boletos. ✅
- Gerador de documentos fiscais (PDFs) incluindo layout de boleto FEBRABAN e NF-e (DANFE simplificado), pronto para integração com bibliotecas como Apache PDFBox. ✅
- Sistema de segurança reforçado (detecção de injeções SQL/XSS, auditoria, criptografia AES, controle de sessões e bloqueios de IP/usuário). ✅
- Arquivos SQL e scripts de criação das tabelas de configuração e segurança preparados para execução no ambiente PROVA1. ✅

Nota de segurança (importante)
-----------------------------
- Este README não divulga senhas, chaves, ou dados sensíveis.
- Para ambiente de produção, configure segredos com um cofre seguro (Vault/HSM/serviço gerenciado) e não inclua credenciais em arquivos versionados.

Arquitetura & Ferramentas
-------------------------
- Backend: Java 17 (JDBC), DAO Pattern
- UI: Java Swing (Material-inspired UI components)
- Banco de Dados: MySQL 8.0 (schema: PROVA1)
- Bibliotecas: ZXing (QR Codes), BCrypt (hash de senha), PDF generator (recomendo Apache PDFBox), MySQL Connector/J
- Ferramentas: DBeaver para administração de banco e inspeção visual

Conexão com o banco (DBeaver)
-----------------------------
Para conectar via DBeaver ao banco PROVA1, use as seguintes informações (não inclua a senha no README):

  - Host: 127.0.0.1
  - Porta: 3306
  - Database: PROVA1
  - Usuário: root
  - Senha: inserir manualmente quando solicitado (NÃO inserir senha aqui)

Observação: Caso utilize um container ou servidor remoto, substitua o host conforme apropriado e siga políticas de segurança ao armazenar credenciais.

Instalação e Build
------------------
Requisitos:
- JDK 17
- Maven (opcional; build.ps1 usa javac caso o Maven não esteja presente)
- MySQL 8.0 (com database PROVA1 criado)

Compilar e executar (Windows PowerShell):
```powershell
cd "<repo-root>"
.\n+\build.ps1
```

Scripts SQL importantes
-----------------------
- `avaliacao1/schema.sql` — script base para criar banco/tabelas originais.
- `avaliacao1/create_sistema_config.sql` — cria a tabela `sistema_config` e populates configurações iniciais (alíquotas, emissor, bancárias, etc.).
- `avaliacao1/create_security_simple.sql` — cria tabelas de segurança (auditoria, sessões, bloqueios, alertas).
- `avaliacao1/schema_impostos.sql` — tabela/modelagem para sistema de impostos da reforma tributária (CBS/IBS/IS).

Segurança (O que foi implementado)
---------------------------------
- Detecção e sanitização de entradas para mitigar SQL Injection (SecurityManager). 
- Proteção contra XSS (sanitização de dados que podem ser renderizados em UI).
- Criptografia AES-256-GCM para dados sensíveis: opções para criptografar/configurar em `db.properties` ou diretamente em um cofre de segredos. 
- Auditoria completa: `auditoria_log` grava ações, e `login_attempts` registra tentativas de login para proteção contra força bruta.
- Sessões: `sessoes_ativas` com tokens de sessão e possibilidade de encerrar sessões ativas.
- Bloqueios automáticos: bloqueio de usuários/IPs (tabela `bloqueios_seguranca`) quando detectada tentativa de brute-force.

Front-end & UX
--------------
- Tela de configurações modernizada: material-inspired, com abas para impostos, dados do emissor, bancário e sistema; validações de entrada e mudança de cores/focus para melhor experiência.
- Dashboard gráfico atualizado com animações e tooltips para facilitar interpretação dos dados financeiros.
- Botões, cards e campos com foco para facilitar sais e inputs de dados.

Integração com o sistema tributário
-----------------------------------
- Inclusão de regras e tabelas para o novo modelo tributário (Reforma Tributária EC132/2023). 
- Tabelas e classes: `ImpostoConfig`, `NotaFiscal`, `Boleto` e `SistemaConfigDAO` foram adicionadas para suporte à geração de documentos conforme o novo regime.
- Alíquotas padrão no sistema:
  - CBS (federal): 8.8%
  - IBS (estadual/municipal): 17.7%
  - Valores e regras estão usáveis e podem ser alterados via `TelaAdminSettings`.

Requisitos Funcionais (Principais)
---------------------------------
1. Autenticação de usuário (login com senha hashed) e roles (admin/usuário).
2. Registro de transações, categorias e cartões.
3. Dashboard com gráficos e métricas snapshot (receitas, despesas, saldo, total transações).
4. Emissão de NF-e / NFS-e e geração de boletos FEBRABAN com linha digitável e código de barras.
5. Integração com PIX (QR code gerado para pagamentos) e leitura/geração de payload.
6. Cálculo automático de tributos (CBS/IBS/IS) por item na nota fiscal e resumo de tributos.
7. Auditoria e logs de segurança para rastreabilidade e compliance.

Requisitos Não Funcionais (Principais)
------------------------------------
1. Segurança: criptografia de dados sensíveis, detecção de SQL Injection e XSS, bloqueio de ataques de força bruta.
2. Confiabilidade: auditoria de eventos críticos, histórico de alterações e copias de segurança.
3. Usabilidade: interface clara e responsiva em Desktop (Swing) com layout moderno e acessível.
4. Portabilidade: suporte a MySQL 8, possibilidade de usar outros drivers JDBC.
5. Manutenibilidade: estrutura modular (DAO, services, UI) e documentação em arquivos `docs/`.

Boas Práticas de Operação
-------------------------
- Proteja credenciais: cadastre senhas no arquivo `db.properties` apenas no servidor ou utilize variáveis de ambiente/cofre de segredos. 
- Event logging e SIEM: direcione logs críticos para solução de SIEM corporativa para monitoramento e análise de invasões.
- Backup: configure backups automáticos do schema/DB para prevenir perda de dados críticos.
- Atualização de dependências de segurança: monitore e atualize bibliotecas (ZXing, MySQL driver, etc.).

Onde procurar código/arquivos relevantes
---------------------------------------
- `src/main/java/controle/security/` — classes de segurança (SecurityManager, AuditoriaDAO, SecureConfigLoader).
- `src/main/java/controle/ui/` — telas e dialogs (TelaAdminSettings, TelaGrafico, TelaPagamento...).
- `src/main/java/controle/dao/` — DAOs (UsuarioDAO, SistemaConfigDAO, UsuarioDAOImpl).
- `avaliacao1/` — scripts SQL para criação de esquema, impostos e tabelas de segurança.

Como contribuir / desenvolvimento
--------------------------------
1. Faça um fork ou clone do repositório.
2. Crie uma branch para a sua feature: `git checkout -b feature/minha-feature`.
3. Siga a convenção de commits: `feat:`, `fix:`, `refactor:` e `docs:`.
4. Teste localmente com `build.ps1` ou Maven (se for utilizar Maven).
5. Abra um PR com a descrição das mudanças e um resumo dos possíveis impactos.

Licença
-------
Este projeto é apenas um exemplo/discursivo e não se destina a produção sem auditoria completa e adaptação a requisitos legais locais. Verifique conformidade com LGPD e legislação fiscal vigente.

Contato
Para dúvidas sobre a arquitetura, segurança ou funcionamento, contate o responsável pelo projeto (ver repositório) — não compartilhe credenciais em fóruns públicos.

---
Última atualização: 07/12/2025
Projeto: Controle Financeiro (esqueleto)

Estrutura criada:
- avaliacao1/schema.sql -> script para criar banco e tabelas
- src/Conexao.java -> utilitários de conexão JDBC
- src/model/Usuario.java, Categoria.java, Transacao.java -> modelos simples
- src/dao/UsuarioDAO.java, UsuarioDAOImpl.java -> exemplo de DAO com CRUD
- src/Main.java -> exemplo de uso (listar/checar conexão)





python .\tools\mock_sync_server.py

Segurança e configuração local
-------------------------------
1. Copie `config/db.properties.example` para `config/db.properties` e adicione suas credenciais locais do banco de dados (não comite este arquivo).
2. Copie `config/api.properties.example` para `config/api.properties` e adicione segredos/credenciais da API (não comite este arquivo).
3. Instale os hooks do pre-commit para detectar segredos antes de comitar (`pip install pre-commit && pre-commit install`).
4. Se você acidentalmente comitar um segredo, deve rotacionar a credencial imediatamente e considerar limpar o histórico usando `git-filter-repo` ou BFG.
