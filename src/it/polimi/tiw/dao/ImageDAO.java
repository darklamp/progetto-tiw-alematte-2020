package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Image;
import org.apache.commons.lang3.StringEscapeUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ImageDAO {
    private final Connection connection;

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
                        image.setSource(StringEscapeUtils.unescapeJava(result.getString("source")));
                        image.setRegion(StringEscapeUtils.unescapeJava(result.getString("region")));
                        image.setTown(StringEscapeUtils.unescapeJava(result.getString("town")));
                        image.setUrl(result.getString("url"));
                        images.add(image);
                    }
                    return images;
                }
            }
        }
    }

    public int getImagesNumber(int campaignId) throws SQLException{
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

    public Image getImage(int imageId) throws SQLException, NoSuchElementException {
        String query = "SELECT * FROM image WHERE id=?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, imageId);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results
                    throw new NoSuchElementException("No image found with id "+imageId);
                else {
                    result.next();
                    Image image = new Image();
                    image.setId(result.getInt("id"));
                    image.setDate(result.getDate("date"));
                    image.setLatitude(result.getFloat("latitude"));
                    image.setLongitude(result.getFloat("longitude"));
                    image.setResolution(result.getString("resolution"));
                    image.setSource(StringEscapeUtils.unescapeJava(result.getString("source")));
                    image.setRegion(StringEscapeUtils.unescapeJava(result.getString("region")));
                    image.setTown(StringEscapeUtils.unescapeJava(result.getString("town")));
                    image.setUrl(result.getString("url"));
                    return image;
                }
            }
        }
    }

    public void insertNewImage(int campaignId, Image image) throws SQLException{
        connection.setAutoCommit(false);
        String query = "INSERT INTO image (date, latitude, longitude, resolution, source, region, town, url) values (NOW(),?,?,?,?,?,?,?)";
        String secondQuery = "INSERT INTO imageCampaign (campaignId, imageId) VALUES (?,?)";
        int imageId = 0;
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);){
            statement.setFloat(1,image.getLatitude());
            statement.setFloat(2,image.getLongitude());
            statement.setString(3, image.getResolution());
            statement.setString(4, StringEscapeUtils.escapeJava(image.getSource()));
            statement.setString(5, StringEscapeUtils.escapeJava(image.getRegion()));
            statement.setString(6, StringEscapeUtils.escapeJava(image.getTown()));
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
        connection.commit();
        connection.setAutoCommit(true);

    }

    public void updateImageMetadata(Image image) throws SQLException{
        String query = "UPDATE image SET latitude=?, longitude=?, resolution=?, source=?, region=?, town=? WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setFloat(1,image.getLatitude());
            statement.setFloat(2,image.getLongitude());
            statement.setString(3, image.getResolution());
            statement.setString(4, StringEscapeUtils.escapeJava(image.getSource()));
            statement.setString(5, StringEscapeUtils.escapeJava(image.getRegion()));
            statement.setString(6, StringEscapeUtils.escapeJava(image.getTown()));
            statement.setInt(7, image.getId());
            statement.executeUpdate();
        }
    }

    public List<Integer> getConflictualCampaignImages(int campaignId) throws SQLException{
        String query = "select imageId from imageCampaign where campaignId= ? and imageId in (select imageId from annotation where validity=1) and imageId in (select imageId from annotation where validity=0) group by imageId";
        List<Integer> results = new ArrayList<>();
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, campaignId);
            try (ResultSet result = pstatement.executeQuery();) {
                    while(result.next()) {
                        int imageId = result.getInt("imageId");
                        results.add(imageId);
                    }
                    return results;
            }
        }
    }


}
