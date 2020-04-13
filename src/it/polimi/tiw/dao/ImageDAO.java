package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Image;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImageDAO {
    private Connection connection;

    public ImageDAO(Connection connection){
        this.connection = connection;
    }

    public List<Image> getCampaignImages(int campaignId) throws SQLException {
        List<Image> images = new ArrayList<>();
        String query = "SELECT i.id, i.date, i.latitude, i.longitude, i.resolution, i.source, i.region, i.town, i.url FROM image AS i JOIN imageCampaign AS iC ON iC.imageId = i.id WHERE iC.campaignId = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, campaignId);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, credential check failed
                    return null;
                else {
                    while(result.next()) {
                        Image image = new Image();
                        image.setId(result.getInt("id"));
                        image.setDate(result.getDate("date"));
                        image.setLatitude(result.getFloat("latitude"));
                        image.setLongitude(result.getFloat("longitude"));
                        image.setResolution(result.getString("resolution"));
                        image.setSource(result.getString("source"));
                        image.setRegion(result.getString("region"));
                        image.setTown(result.getString("town"));
                        image.setUrl(result.getString("url"));
                        images.add(image);
                    }
                    return images;
                }
            }
        }
    }

    public int getLastImageIndex(int campaignId) throws SQLException{
        int index = 0;
        String query = "SELECT count(*) AS 'lastindex' FROM imageCampaign WHERE campaignId=?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1,campaignId);
            try (ResultSet result = statement.executeQuery();) {
                result.next();
                index = result.getInt(1);
            }
        }
        return index;
    }

    public Image getImage(int imageId) throws SQLException{
        String query = "SELECT * FROM image WHERE id=?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, imageId);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results
                    return null;
                else {
                    result.next();
                    Image image = new Image();
                    image.setId(result.getInt("id"));
                    image.setDate(result.getDate("date"));
                    image.setLatitude(result.getFloat("latitude"));
                    image.setLongitude(result.getFloat("longitude"));
                    image.setResolution(result.getString("resolution"));
                    image.setSource(result.getString("source"));
                    image.setRegion(result.getString("region"));
                    image.setTown(result.getString("town"));
                    image.setUrl(result.getString("url"));
                    return image;
                }
            }
        }
    }

    public void insertNewImage(int campaignId, Image image) throws SQLException{
        String query = "INSERT INTO image (date, latitude, longitude, resolution, source, region, town, url) values (NOW(),?,?,?,?,?,?,?)";
        String secondQuery = "INSERT INTO imageCampaign (campaignId, imageId) VALUES (?,?)";
        int imageId = 0;
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);){
            statement.setFloat(1,image.getLatitude());
            statement.setFloat(2,image.getLongitude());
            statement.setString(3, image.getResolution());
            statement.setString(4, image.getSource());
            statement.setString(5, image.getRegion());
            statement.setString(6, image.getTown());
            statement.setString(7, image.getUrl());
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if(rs.next()){
                imageId = (int) rs.getLong(1);
            } else {
                throw new SQLException();
            }
        }
        try(PreparedStatement stmt = connection.prepareStatement(secondQuery)){
            stmt.setInt(1, campaignId);
            stmt.setInt(2, imageId);
            stmt.executeUpdate();
        }

    }

}
