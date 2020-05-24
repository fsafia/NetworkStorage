package network.storage.server;

import java.sql.*;

public class AuthService {
    private static Connection connection;
    private static Statement stmt;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            stmt = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPass(String login, String password) {
//        String sql = String.format("select nickname, password from users where login = '%s'", login);
        String sql = String.format("select nick from users where login = '%s' and password = '%s'", login, password);
        ResultSet rs = null; //по нашему запросу может вернуться 1 конкретная строка или 0
        try {
            rs = stmt.executeQuery(sql);
//            int myHash = password.hashCode();
//            if (rs.next()) { //если вернется 1 строка ->усл выполн, если вернется 0 строк в if не зайдет
                String nick = rs.getString(1);
//                int dbHash = rs.getInt(2);
//                if (myHash == dbHash) {
                    return nick;
//                }
                //return  rs.getString(1); //при работе с jdbc индексация начин с 1
//            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
