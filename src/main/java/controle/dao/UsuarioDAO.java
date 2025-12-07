package controle.dao;

import java.util.List;

import controle.model.Usuario;

public interface UsuarioDAO {

    Usuario insert(Usuario u) throws Exception;

    Usuario findById(int id) throws Exception;

    List<Usuario> findAll() throws Exception;

    Usuario findByEmail(String email) throws Exception;

    Usuario findByNome(String nome) throws Exception;

    boolean update(Usuario u) throws Exception;

    boolean delete(int id) throws Exception;
}
