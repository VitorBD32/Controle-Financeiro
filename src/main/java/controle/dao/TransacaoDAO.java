package controle.dao;

import java.time.LocalDate;
import java.util.List;

import controle.model.Transacao;

public interface TransacaoDAO {

    Transacao insert(Transacao t) throws Exception;

    Transacao findById(int id) throws Exception;

    List<Transacao> findAll() throws Exception;

    List<Transacao> findByPeriodo(LocalDate inicio, LocalDate fim) throws Exception;

    boolean update(Transacao t) throws Exception;

    boolean delete(int id) throws Exception;
}
