package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Campaign;
import it.polimi.tiw.beans.Image;
import it.polimi.tiw.beans.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CampaignDAO {
    private Connection connection;

    public CampaignDAO(Connection connection){this.connection=connection;}

    public long createCampaign(int managerId, String name, String client) throws SQLException {
        String query = "INSERT INTO campaign (managerId, name, client, state) VALUES (?,?,?,'created')";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);){
            statement.setInt(1, managerId);
            statement.setString(2, name);
            statement.setString(3, client);
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if(rs.next()){
                return rs.getLong(1);
            }
        }
        return 0;
    }

    public List<Campaign> getManagerCampaigns(int managerId) throws SQLException{
        List<Campaign> result = new ArrayList<>();
        String query = "SELECT * FROM campaign WHERE managerId = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, managerId);
            try (ResultSet resultSet = pstatement.executeQuery();) {
                if (!resultSet.isBeforeFirst()) // no results
                    return null;
                else {
                    while(resultSet.next()) {
                        Campaign campaign = new Campaign();
                        campaign.setId(resultSet.getInt("id"));
                        campaign.setName(resultSet.getString("name"));
                        campaign.setClient(resultSet.getString("client"));
                        campaign.setState(resultSet.getString("state"));
                        result.add(campaign);
                    }
                }
            }
        }
        return result;
    }

    public List<Campaign> getWorkerCampaigns(int workerId) throws SQLException{
        List<Campaign> result = new ArrayList<>();
        String query = "SELECT * FROM campaign join workerCampaign on campaign.id = workerCampaign.campaignId WHERE workerId = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, workerId);
            try (ResultSet resultSet = pstatement.executeQuery();) {
                if (!resultSet.isBeforeFirst()) // no results
                    return null;
                else {
                    while(resultSet.next()) {
                        Campaign campaign = new Campaign();
                        campaign.setId(resultSet.getInt("id"));
                        campaign.setName(resultSet.getString("name"));
                        campaign.setClient(resultSet.getString("client"));
                        campaign.setState(resultSet.getString("state"));
                        result.add(campaign);
                    }
                }
            }
        }
        return result;
    }

    public List<Campaign> getWorkerAvailableCampaigns(int workerId) throws SQLException{
        List<Campaign> result = new ArrayList<>();
        String query = "SELECT id,managerId,name,client,state FROM campaign EXCEPT select id,managerId,name,client,state FROM campaign join workerCampaign on campaign.id = workerCampaign.campaignId WHERE workerId = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, workerId);
            try (ResultSet resultSet = pstatement.executeQuery();) {
                if (!resultSet.isBeforeFirst()) // no results
                    return null;
                else {
                    while(resultSet.next()) {
                        Campaign campaign = new Campaign();
                        campaign.setId(resultSet.getInt("id"));
                        campaign.setName(resultSet.getString("name"));
                        campaign.setClient(resultSet.getString("client"));
                        campaign.setState(resultSet.getString("state"));
                        result.add(campaign);
                    }
                }
            }
        }
        return result;
    }

    public void setState(int campaignId, String state) throws SQLException{
        String query = "UPDATE campaign SET state = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);){
            statement.setString(1, state);
            statement.setInt(2, campaignId);
            statement.executeUpdate();
        }
    }

    public Campaign getCampaignById(int campaignId) throws SQLException{
        String query = "SELECT * FROM campaign WHERE id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, campaignId);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, credential check failed
                    return null;
                else {
                    result.next();
                    Campaign campaign = new Campaign();
                    campaign.setId(campaignId);
                    campaign.setName(result.getString("name"));
                    campaign.setClient(result.getString("client"));
                    campaign.setState(result.getString("state"));
                    return campaign;
                }
            }
        }
    }

    public boolean inNameFree(String name) throws SQLException{
        String query = "SELECT 1 FROM campaign WHERE name=?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setString(1, name);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, credential check failed
                    return true;
                return false;
            }
        }
    }

    public List<Image> getCampaignImages(int campaignId) throws SQLException{
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
