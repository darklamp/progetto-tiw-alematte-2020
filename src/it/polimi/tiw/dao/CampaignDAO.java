package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Campaign;
import it.polimi.tiw.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CampaignDAO {
    private Connection connection;

    public CampaignDAO(Connection connection){this.connection=connection;}

    public void createCampaign(int managerId, String name, String client) throws SQLException {
        String query = "INSERT INTO campaign (managerId, name, client, state) VALUES (?,?,?,'created')";
        try (PreparedStatement statement = connection.prepareStatement(query);){
            statement.setInt(1, managerId);
            statement.setString(2, name);
            statement.setString(3, client);
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

}
