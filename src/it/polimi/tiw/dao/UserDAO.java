package it.polimi.tiw.dao;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.utility.Crypto;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;

public class UserDAO {
    private final Connection con;

    /* TODO: fare sanitization di molti input */

    public UserDAO(Connection connection) {
        this.con = connection;
    }

    private String getUserSalt(String username) throws SQLException, NoSuchElementException {
        String query = "SELECT salt FROM user WHERE username = ?";
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setString(1, StringEscapeUtils.escapeJava(username));
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
        String query = "UPDATE user SET authcookie=?,cookietime=NOW() WHERE id=?";
        String cookieValue = cookie.getValue();
        String hashedValue = Crypto.cookieHash(cookieValue);
        try (PreparedStatement statement = con.prepareStatement(query);){
            statement.setString(1, hashedValue);
            statement.setInt(2, u.getId());
            statement.executeUpdate();
        }
    }

    public void deleteCookie(User user){
        int id = user.getId();
        Date fakeDate = new Date(1);
        String date = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(fakeDate);

        String query = "UPDATE user SET authcookie=?,cookietime=? WHERE id=?";
        try (PreparedStatement statement = con.prepareStatement(query);){
            statement.setNull(1, Types.VARCHAR);
            statement.setString(2, date);
            statement.setInt(3, id);
            statement.executeUpdate();
        } catch (SQLException ignored) {
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
                    Date then = result.getTimestamp("cookietime");
                    Date now = new Date();
                    if(then == null) throw new NoSuchElementException();
                    long seconds = (now.getTime()-then.getTime())/1000;
                    if (seconds < 0 || seconds > 3600){
                        throw new NoSuchElementException();
                    }
                    User user = new User();
                    if(result.getString("role").equals("worker")){
                        user.setImageURL(StringEscapeUtils.unescapeJava(result.getString("photo")));
                        user.setLevel(result.getString("level"));
                    }
                    user.setId(result.getInt("id"));
                    user.setRole(result.getString("role"));
                    user.setUsername(StringEscapeUtils.unescapeJava(result.getString("username")));
                    user.setEmail(StringEscapeUtils.unescapeJava(result.getString("email")));
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
            pstatement.setString(1, StringEscapeUtils.escapeJava(username));
            pstatement.setString(2, hash);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, credential check failed
                    throw new NoSuchElementException();
                else {
                    result.next();
                    User user = new User();
                    if(result.getString("role").equals("worker")){
                        user.setImageURL(StringEscapeUtils.unescapeJava(result.getString("photo")));
                        user.setLevel(result.getString("level"));
                    }
                    user.setId(result.getInt("id"));
                    user.setRole(result.getString("role"));
                    user.setUsername(StringEscapeUtils.unescapeJava(result.getString("username")));
                    user.setEmail(StringEscapeUtils.unescapeJava(result.getString("email")));
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
                        user.setImageURL(StringEscapeUtils.unescapeJava(result.getString("photo")));
                        user.setLevel(result.getString("level"));
                    }
                    user.setId(result.getInt("id"));
                    user.setRole(result.getString("role"));
                    user.setUsername(StringEscapeUtils.unescapeJava(result.getString("username")));
                    user.setEmail(StringEscapeUtils.unescapeJava(result.getString("email")));
                    return user;
                }
            }
        }
    }

    public void updateUser(User user) throws SQLException{
        String query = "UPDATE user SET username=?, email=?, level=?, photo=? WHERE id=?";
        try (PreparedStatement statement = con.prepareStatement(query);){
            statement.setString(1, StringEscapeUtils.escapeJava(user.getUsername()));
            statement.setString(2, StringEscapeUtils.escapeJava(user.getEmail()));
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
        String query = "INSERT INTO user (username, email, password, role, level, photo, salt, authcookie, cookietime) VALUES (?, ?, ?, ?, null, null, ?, null, null)";
        String salt = Crypto.createSalt();
        String hash = Crypto.pwHash(password,salt.getBytes(StandardCharsets.UTF_8));
        try (PreparedStatement statement = con.prepareStatement(query);){
            statement.setString(1, StringEscapeUtils.escapeJava(username));
            statement.setString(2, StringEscapeUtils.escapeJava(email));
            statement.setString(3, hash);
            statement.setString(4, role);
            statement.setString(5, salt);
            statement.executeUpdate();
        }
    }

    public void addWorkerUser(String username, String email, String password, String role, String experience, String photo) throws SQLException{
        String query = "INSERT INTO user (username, email, password, role, level, photo, salt, authcookie, cookietime) VALUES (?, ?, ?, ?, ?, ?, ?, null, null)";
        String salt = Crypto.createSalt();
        String hash = Crypto.pwHash(password,salt.getBytes(StandardCharsets.UTF_8));
        try (PreparedStatement statement = con.prepareStatement(query);){
            statement.setString(1, StringEscapeUtils.escapeJava(username));
            statement.setString(2, StringEscapeUtils.escapeJava(email));
            statement.setString(3, hash);
            statement.setString(4, role);
            statement.setString(5, experience);
            statement.setString(6, StringEscapeUtils.escapeJava(photo));
            statement.setString(7, salt);
            statement.executeUpdate();
        }
    }

    public boolean isUsernameFree(String username) throws SQLException{
        String query = "SELECT 1 FROM user WHERE username= ?";
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setString(1, StringEscapeUtils.escapeJava(username));
            try (ResultSet result = pstatement.executeQuery();) {
                // no results, credential check failed
                return !result.isBeforeFirst();
            }
        }
    }

    public boolean isEmailFree(String email) throws SQLException{
        String query = "SELECT 1 FROM user WHERE email=?";
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setString(1, StringEscapeUtils.escapeJava(email));
            try (ResultSet result = pstatement.executeQuery();) {
                // no results, credential check failed
                return !result.isBeforeFirst();
            }
        }
    }

    public boolean alreadyExists(String username, String email) throws SQLException{
        String query = "SELECT 1 FROM user WHERE username= ? AND email= ?";
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setString(1, StringEscapeUtils.escapeJava(username));
            pstatement.setString(2, StringEscapeUtils.escapeJava(email));
            try (ResultSet result = pstatement.executeQuery();) {
                // no results, credential check failed
                return result.isBeforeFirst();
            }
        }
    }
}

