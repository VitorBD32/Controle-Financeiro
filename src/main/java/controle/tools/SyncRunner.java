package controle.tools;

import controle.dao.TransacaoDAOImpl;

public class SyncRunner {
    public static void main(String[] args) {
        TransacaoDAOImpl dao = new TransacaoDAOImpl();
        String result = dao.syncToAPI();
        System.out.println("SyncRunner result:\n" + result);
    }
}
