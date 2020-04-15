package it.polimi.tiw.dao;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.NoSuchElementException;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utility.Crypto;
import org.thymeleaf.model.IStandaloneElementTag;

import javax.servlet.http.Cookie;

import static de.mkammerer.argon2.Argon2Factory.*;

public class UserDAO {
    private Connection con;

    /* TODO: fare sanitization di molti input */

    public UserDAO(Connection connection) {
        this.con = connection;
    }

    private String getUserSalt(String username) throws SQLException, NoSuchElementException {
        String query = "SELECT salt FROM user WHERE username = ?";
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setString(1, username);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) throw new NoSuchElementException();
                else {
                    result.next();
                    return result.getString("salt");
                }
            }

        }
    }
    private String getUserSalt(int userId) throws SQLException, NoSuchElementException{
        String query = "SELECT salt FROM user WHERE id = ?";
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setInt(1, userId);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) throw new NoSuchElementException();
                else {
                    result.next();
                    return result.getString("salt");
                }
            }

        }
    }

    public void addCookie(User u, Cookie cookie) throws SQLException{
        String query = "UPDATE user SET authcookie=? WHERE id=?";
        String cookieValue = cookie.getValue();
        String hashedValue = Crypto.cookieHash(cookieValue);
        try (PreparedStatement statement = con.prepareStatement(query);){
            statement.setString(1, hashedValue);
            statement.setInt(2, u.getId());
            statement.executeUpdate();
        }
    }

    public User getUserFromCookie(String cookie) throws SQLException{
        String query = "SELECT * FROM user WHERE authcookie=?";
        String hashedValue = Crypto.cookieHash(cookie);
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setString(1, hashedValue);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, credential check failed
                    throw new NoSuchElementException();
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

    public User checkCredentials(String username, String password) throws SQLException,NoSuchElementException {

        String salt = getUserSalt(username);
        String query = "SELECT  * FROM user WHERE username = ? AND password = ?";
        String hash = Crypto.pwHash(password,salt.getBytes(StandardCharsets.UTF_8));
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setString(1, username);
            pstatement.setString(2, hash);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, credential check failed
                    throw new NoSuchElementException();
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

    public User getUser(int userId) throws SQLException, NoSuchElementException {
        String query = "SELECT  * FROM user WHERE id = ?";
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setInt(1, userId);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, credential check failed
                    throw new NoSuchElementException();
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

    public void updateUserPassword(int id, String newPassword) throws SQLException{
        String query = "UPDATE user SET password=? WHERE id=?";
        String salt = getUserSalt(id);
        String hash = Crypto.pwHash(newPassword,salt.getBytes(StandardCharsets.UTF_8));
        try (PreparedStatement statement = con.prepareStatement(query);){
            statement.setString(1, hash);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }


    public void addManagerUser(String username, String email, String password, String role) throws SQLException{
        String query = "INSERT INTO user (username, email, password, role, level, photo, salt) VALUES (?, ?, ?, ?, null, null, ?)";
        String salt = Crypto.createSalt();
        String hash = Crypto.pwHash(password,salt.getBytes(StandardCharsets.UTF_8));
        try (PreparedStatement statement = con.prepareStatement(query);){
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, hash);
            statement.setString(4, role);
            statement.setString(5, salt);
            statement.executeUpdate();
        }
    }

    public void addWorkerUser(String username, String email, String password, String role, String experience, String photo) throws SQLException{
        String query = "INSERT INTO user (username, email, password, role, level, photo, salt) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String salt = Crypto.createSalt();
        String hash = Crypto.pwHash(password,salt.getBytes(StandardCharsets.UTF_8));
        try (PreparedStatement statement = con.prepareStatement(query);){
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, hash);
            statement.setString(4, role);
            statement.setString(5, experience);
            statement.setString(6, photo);
            statement.setString(7, salt);
            statement.executeUpdate();
        }
    }

    public boolean isUsernameFree(String username) throws SQLException{
        String query = "SELECT 1 FROM user WHERE username= ?";
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setString(1, username);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, credential check failed
                    return true;
                return false;
            }
        }
    }

    public boolean isEmailFree(String email) throws SQLException{
        String query = "SELECT 1 FROM user WHERE email=?";
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setString(1, email);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, credential check failed
                    return true;
                return false;
            }
        }
    }

    public boolean alreadyExists(String username, String email) throws SQLException{
        String query = "SELECT 1 FROM user WHERE username= ? AND email= ?";
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setString(1, username);
            pstatement.setString(2, email);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, credential check failed
                    return false;
                return true;
            }
        }
    }
}

