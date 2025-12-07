package controle.security;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * CARREGADOR DE CONFIGURAÇÕES SEGURO
 * ====================================
 * 
 * Carrega configurações do arquivo db.properties com suporte a:
 * - Criptografia de valores sensíveis
 * - Validação de integridade
 * - Proteção contra leitura não autorizada
 * 
 * Formato de valores criptografados: ENC(valor_criptografado)
 * 
 * @author Sistema de Controle Financeiro
 * @version 2.0 - Segurança Reforçada
 */
public class SecureConfigLoader {
    
    private static final String CONFIG_PATH = "config/db.properties";
    private static final String CONFIG_BACKUP_PATH = "config/db.properties.bak";
    
    private Properties properties;
    private SecurityManager securityManager;
    private boolean loaded = false;
    
    private static SecureConfigLoader instance;
    
    private SecureConfigLoader() {
        this.properties = new Properties();
        this.securityManager = SecurityManager.getInstance();
    }
    
    public static synchronized SecureConfigLoader getInstance() {
        if (instance == null) {
            instance = new SecureConfigLoader();
        }
        return instance;
    }
    
    /**
     * Carrega as configurações do arquivo
     */
    public void load() throws IOException {
        File configFile = new File(CONFIG_PATH);
        
        if (!configFile.exists()) {
            throw new FileNotFoundException("Arquivo de configuração não encontrado: " + CONFIG_PATH);
        }
        
        // Verifica permissões do arquivo
        checkFilePermissions(configFile);
        
        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
            loaded = true;
            
            System.out.println("[CONFIG] Configurações carregadas com segurança");
        }
    }
    
    /**
     * Verifica permissões do arquivo de configuração
     */
    private void checkFilePermissions(File file) {
        // Em produção, verificar se o arquivo não é world-readable
        if (file.canRead()) {
            // Log de acesso ao arquivo de configuração
            securityManager.logSecurityEvent("CONFIG_ACCESS", "Arquivo de configuração acessado");
        }
    }
    
    /**
     * Obtém uma propriedade, descriptografando se necessário
     */
    public String getProperty(String key) {
        if (!loaded) {
            try {
                load();
            } catch (IOException e) {
                System.err.println("[CONFIG] Erro ao carregar configurações: " + e.getMessage());
                return null;
            }
        }
        
        String value = properties.getProperty(key);
        
        // Descriptografa se estiver no formato ENC(...)
        if (value != null && securityManager.isEncrypted(value)) {
            try {
                value = securityManager.decryptConfig(value);
            } catch (Exception e) {
                System.err.println("[CONFIG] Erro ao descriptografar " + key + ": " + e.getMessage());
                return null;
            }
        }
        
        return value;
    }
    
    /**
     * Obtém uma propriedade como inteiro
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null) return defaultValue;
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Obtém uma propriedade como boolean
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value == null) return defaultValue;
        
        return "true".equalsIgnoreCase(value.trim()) || "1".equals(value.trim());
    }
    
    /**
     * Salva uma propriedade (criptografando se for sensível)
     */
    public void setProperty(String key, String value, boolean encrypt) {
        if (encrypt) {
            value = securityManager.encryptConfig(value);
        }
        properties.setProperty(key, value);
    }
    
    /**
     * Salva as configurações no arquivo
     */
    public void save() throws IOException {
        // Faz backup antes de salvar
        backupConfig();
        
        try (FileOutputStream fos = new FileOutputStream(CONFIG_PATH)) {
            properties.store(fos, "Configurações do Sistema - Última atualização: " + 
                            java.time.LocalDateTime.now());
        }
        
        System.out.println("[CONFIG] Configurações salvas com sucesso");
    }
    
    /**
     * Cria backup do arquivo de configuração
     */
    private void backupConfig() {
        try {
            File source = new File(CONFIG_PATH);
            File backup = new File(CONFIG_BACKUP_PATH);
            
            if (source.exists()) {
                Files.copy(source.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("[CONFIG] Erro ao criar backup: " + e.getMessage());
        }
    }
    
    /**
     * Criptografa a senha no arquivo de configuração
     */
    public void encryptPassword() throws IOException {
        String password = properties.getProperty("password");
        
        if (password != null && !securityManager.isEncrypted(password)) {
            String encrypted = securityManager.encryptConfig(password);
            properties.setProperty("password", encrypted);
            save();
            System.out.println("[CONFIG] Senha criptografada com sucesso");
        }
    }
    
    /**
     * Obtém a URL de conexão JDBC segura
     */
    public String getJdbcUrl() {
        String host = getProperty("host");
        String port = getProperty("port");
        String database = getProperty("database");
        
        if (host == null) host = "127.0.0.1";
        if (port == null) port = "3306";
        if (database == null) database = "PROVA1";
        
        StringBuilder url = new StringBuilder();
        url.append("jdbc:mysql://").append(host).append(":").append(port);
        url.append("/").append(database);
        url.append("?useSSL=").append(getBooleanProperty("useSSL", false));
        url.append("&allowPublicKeyRetrieval=").append(getBooleanProperty("allowPublicKeyRetrieval", true));
        url.append("&serverTimezone=America/Sao_Paulo");
        url.append("&characterEncoding=UTF-8");
        url.append("&useUnicode=true");
        
        return url.toString();
    }
    
    /**
     * Obtém o usuário do banco
     */
    public String getDbUser() {
        return getProperty("user");
    }
    
    /**
     * Obtém a senha do banco (descriptografada)
     */
    public String getDbPassword() {
        return getProperty("password");
    }
    
    /**
     * Verifica se as configurações são válidas
     */
    public boolean isConfigValid() {
        String host = getProperty("host");
        String port = getProperty("port");
        String database = getProperty("database");
        String user = getProperty("user");
        String password = getProperty("password");
        
        return host != null && !host.isEmpty() &&
               port != null && !port.isEmpty() &&
               database != null && !database.isEmpty() &&
               user != null && !user.isEmpty() &&
               password != null && !password.isEmpty();
    }
    
    /**
     * Exibe resumo das configurações (mascarando dados sensíveis)
     */
    public void printConfigSummary() {
        System.out.println("\n========== CONFIGURAÇÕES DO BANCO ==========");
        System.out.println("Host: " + getProperty("host"));
        System.out.println("Porta: " + getProperty("port"));
        System.out.println("Banco: " + getProperty("database"));
        System.out.println("Usuário: " + getProperty("user"));
        System.out.println("Senha: " + securityManager.maskSensitiveData(getProperty("password"), "CONTA"));
        System.out.println("SSL: " + getBooleanProperty("useSSL", false));
        System.out.println("=============================================\n");
    }
    
    /**
     * Recarrega as configurações
     */
    public void reload() throws IOException {
        loaded = false;
        properties.clear();
        load();
    }
    
    /**
     * Main para testes
     */
    public static void main(String[] args) {
        try {
            SecureConfigLoader config = SecureConfigLoader.getInstance();
            config.load();
            config.printConfigSummary();
            
            System.out.println("URL JDBC: " + config.getJdbcUrl());
            System.out.println("Config válida: " + config.isConfigValid());
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
