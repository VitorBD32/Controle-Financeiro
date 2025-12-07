package controle.dao;

import controle.model.Cartao;
import java.util.List;

/**
 * Interface DAO para operações com cartões
 */
public interface CartaoDAO {
    
    /**
     * Insere um novo cartão (dados sensíveis já devem estar criptografados)
     */
    void insert(Cartao cartao) throws Exception;
    
    /**
     * Atualiza um cartão existente
     */
    void update(Cartao cartao) throws Exception;
    
    /**
     * Remove (desativa) um cartão por ID
     */
    void delete(int id) throws Exception;
    
    /**
     * Busca cartão por ID
     */
    Cartao findById(int id) throws Exception;
    
    /**
     * Busca cartão por token
     */
    Cartao findByToken(String token) throws Exception;
    
    /**
     * Lista todos os cartões ativos de um usuário
     */
    List<Cartao> findByUsuario(int idUsuario) throws Exception;
    
    /**
     * Lista todos os cartões (admin)
     */
    List<Cartao> findAll() throws Exception;
    
    /**
     * Atualiza data do último uso
     */
    void registrarUso(int id) throws Exception;
}
