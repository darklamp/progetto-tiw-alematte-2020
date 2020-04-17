package it.polimi.tiw.beans;

import it.polimi.tiw.utility.JsonSupport;

public class User implements JsonSupport {
    private int id;
    private String username;
    private String email;
    private String role;
    private String level = null;
    private String imageURL = null;

    public User() {

    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public String convertToJSON() {
        String result ="{";
        result += "\"id\":\""+id+"\",";
        result += "\"username\":\""+username+"\",";
        result += "\"email\":\""+email+"\",";
        result += "\"role\":\""+role+"\",";
        if(level != null){
            result += "\"experience\":\""+level+"\",";
        }
        result += "\"photo\":\""+imageURL+"\"";
        result += "}";
        return result;
    }

}
