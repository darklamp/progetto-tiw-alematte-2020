package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import it.polimi.tiw.beans.User;
import org.thymeleaf.model.IStandaloneElementTag;

public class UserDAO {
    private Connection con;

    public UserDAO(Connection connection) {
        this.con = connection;
    }

    public User checkCredentials(String username, String password) throws SQLException {
        String query = "SELECT  * FROM user  WHERE username = ? AND password =?";
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setString(1, username);
            pstatement.setString(2, password);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, credential check failed
                    return null;
                else {
                    result.next();
                    User user = new User();
                    if(result.getString("role").equals("worker")){
                        user.setImageURL(result.getString("photo"));
                        user.setLevel(result.getString("level"));
                    }
                    user.setId(result.getInt("id"));
                    user.setRole(result.getString("role"));
                    user.setUsername(result.getString("username"));
                    user.setEmail(result.getString("email"));
                    return user;
                }
            }
        }
    }

    public void updateUser(User user) throws SQLException{
        String query = "UPDATE user SET username=?, email=?, level=?, photo=? WHERE id=?";
        try (PreparedStatement statement = con.prepareStatement(query);){
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getLevel());
            statement.setString(4, user.getImageURL());
            statement.setInt(5, user.getId());
            statement.executeUpdate();
        }
    }

    public static void addUser(Connection connection, String username, String email, String password, String role) throws SQLException{
        String query = "INSERT INTO user (username, email, password, role, level, photo) VALUES (?, ?, ?, ?, null, null)";
        try (PreparedStatement statement = connection.prepareStatement(query);){
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, role);
            statement.executeUpdate();
        }
    }
}
