package network.storage.server;

import java.sql.*;
import java.util.Vector;

public class AuthService {
    private static Connection connection;
    private static Statement stmt;

    private static Vector<String> userList = new Vector<>();
    public static Vector<String> getUserList() {
        return userList;
    }


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
        String sql = String.format("select nick, password from users where login = '%s'", login);
        ResultSet rs = null; //по нашему запросу может вернуться 1 конкретная строка или 0
        try {
            rs = stmt.executeQuery(sql);
            int myHash = password.hashCode();
            if (rs.next()) { //если вернется 1 строка ->усл выполн, если вернется 0 строк в if не зайдет
                String nick = rs.getString(1);
                int dbHash = rs.getInt(2);
                if (myHash == dbHash) {
                    return nick;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //для проверки уникальности логина при регистрации в базе данных
    public static boolean isLoginUnique(String login) {
        boolean rezult = true;
        String sql = String.format("select nick from users where login = '%s'", login);
        ResultSet rs = null; //по нашему запросу может вернуться 1 строка или 0
        try {
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                rezult = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rezult;
    }
    //для проверки уникальности nick при регистрации в базе данных
    public static boolean isNickUnique(String nick) {
        String sql = String.format("select login from users where nick = '%s'", nick);
        ResultSet rs = null; //по нашему запросу может вернуться 1 строка или 0
        try {
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    //добавление в таблицу users новых зарегистрированных клиентов
    public static void addUser(String login, String pass, String nick) {
        try {
            String query = "INSERT INTO users (login, password, nick) VALUES (?, ?, ?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, login);
            ps.setInt(2, pass.hashCode());
            ps.setString(3, nick);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
