import data.dao.TransactionDAO;

public class Main {
    public static void main(String[] args) {
        TransactionDAO dao = new TransactionDAO();
        var transactions = dao.findAll();
    }
}
