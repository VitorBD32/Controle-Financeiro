package controle.dao;

public interface ConfigDAO {
    String get(String key) throws Exception;
    boolean set(String key, String value) throws Exception;
}
