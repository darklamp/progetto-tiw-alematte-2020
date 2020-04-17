package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Campaign;
import it.polimi.tiw.beans.Image;
import it.polimi.tiw.beans.User;
import org.apache.commons.lang3.StringEscapeUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class CampaignDAO {
    private Connection connection;

    public CampaignDAO(Connection connection){this.connection=connection;}

    public long createCampaign(int managerId, String name, String client) throws SQLException {
        String query = "INSERT INTO campaign (managerId, name, client, state) VALUES (?,?,?,'created')";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);){
            statement.setInt(1, managerId);
            statement.setString(2, StringEscapeUtils.escapeJava(name));
            statement.setString(3, StringEscapeUtils.escapeJava(client));
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if(rs.next()){
                return rs.getLong(1);
            }
        }
        return 0;
    }

    public void updateCampaign(int campaignId, String name, String client) throws SQLException {
        String query = "UPDATE campaign SET name=?, client=? WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, StringEscapeUtils.escapeJava(name));
            statement.setString(2, StringEscapeUtils.escapeJava(client));
            statement.setInt(3, campaignId);
            statement.executeUpdate();
        }
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
                        campaign.setName(StringEscapeUtils.unescapeJava(resultSet.getString("name")));
                        campaign.setClient(StringEscapeUtils.unescapeJava(resultSet.getString("client")));
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
        String query = "SELECT * FROM campaign AS c JOIN workerCampaign AS wC ON c.id = wC.campaignId WHERE wC.workerId = ? and state='started'";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, workerId);
            try (ResultSet resultSet = pstatement.executeQuery();) {
                if (!resultSet.isBeforeFirst()) // no results
                    return null;
                else {
                    while(resultSet.next()) {
                        Campaign campaign = new Campaign();
                        campaign.setId(resultSet.getInt("id"));
                        campaign.setName(StringEscapeUtils.unescapeJava(resultSet.getString("name")));
                        campaign.setClient(StringEscapeUtils.unescapeJava(resultSet.getString("client")));
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
        String query = "SELECT * FROM campaign as c WHERE state='started' AND c.id NOT IN (SELECT wC.campaignId FROM workerCampaign as wC WHERE wC.workerId=?)";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, workerId);
            try (ResultSet resultSet = pstatement.executeQuery();) {
                if (!resultSet.isBeforeFirst()) // no results
                    return null;
                else {
                    while(resultSet.next()) {
                        Campaign campaign = new Campaign();
                        campaign.setId(resultSet.getInt("id"));
                        campaign.setName(StringEscapeUtils.escapeJava(resultSet.getString("name")));
                        campaign.setClient(StringEscapeUtils.escapeJava(resultSet.getString("client")));
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

    public Campaign getCampaignById(int campaignId) throws SQLException, NoSuchElementException {
        String query = "SELECT * FROM campaign WHERE id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, campaignId);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, credential check failed
                    throw new NoSuchElementException("Campaign not found");
                else {
                    result.next();
                    Campaign campaign = new Campaign();
                    campaign.setId(campaignId);
                    campaign.setName(StringEscapeUtils.unescapeJava(result.getString("name")));
                    campaign.setClient(StringEscapeUtils.unescapeJava(result.getString("client")));
                    campaign.setState(result.getString("state"));
                    return campaign;
                }
            }
        }
    }

    public boolean inNameFree(String name) throws SQLException{
        String query = "SELECT 1 FROM campaign WHERE name=?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setString(1, StringEscapeUtils.escapeJava(name));
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, credential check failed
                    return true;
                return false;
            }
        }
    }

}
