package controle.dao;

import java.util.List;

import controle.model.Categoria;

public interface CategoriaDAO {

    Categoria insert(Categoria c) throws Exception;

    Categoria findById(int id) throws Exception;

    List<Categoria> findAll() throws Exception;

    boolean update(Categoria c) throws Exception;

    boolean delete(int id) throws Exception;
}
