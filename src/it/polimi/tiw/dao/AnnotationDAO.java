package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Annotation;
import it.polimi.tiw.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

public class AnnotationDAO {
    private Connection connection;

    public AnnotationDAO(Connection connection){
        this.connection = connection;
    }

    public void createAnnotation(int workerId, int imageId, int validity, String trust, String note) throws SQLException {
        String query = "INSERT INTO annotation (workerId, imageId, date, validity, trust, note) values (?,?,NOW(),?,?,?)";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, workerId);
            pstatement.setInt(2, imageId);
            pstatement.setInt(3, validity);
            pstatement.setString(4, trust);
            pstatement.setString(5, note);
            pstatement.executeUpdate();
        }
    }

    public Annotation getAnnotation(int workerId, int imageId) throws SQLException {
        String query = "SELECT * FROM annotation WHERE workerId = ? AND imageId = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, workerId);
            pstatement.setInt(2, imageId);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results
                    throw new NoSuchElementException();
                else {
                    result.next();
                    Annotation annotation = new Annotation();
                    annotation.setWorkerId(workerId);
                    annotation.setImageId(imageId);
                    annotation.setDate(result.getDate("date"));
                    annotation.setValidity(result.getInt("validity"));
                    annotation.setTrust(result.getString("trust"));
                    annotation.setNote(result.getString("note"));
                    return annotation;
                }
            }
        }
    }

    public List<Annotation> getAnnotations(int imageId) throws SQLException{
        List<Annotation> annotations = new ArrayList<>();
        String query = "SELECT * FROM annotation WHERE imageId = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, imageId);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results
                    return null;
                else {
                    while (result.next()) {
                        Annotation annotation = new Annotation();
                        annotation.setWorkerId(result.getInt("workerId"));
                        annotation.setImageId(imageId);
                        annotation.setDate(result.getDate("date"));
                        annotation.setValidity(result.getInt("validity"));
                        annotation.setTrust(result.getString("trust"));
                        annotation.setNote(result.getString("note"));
                        annotations.add(annotation);
                    }
                    return annotations;
                }
            }
        }
    }

    public int getNumberOdAnnotationsInCampaign(int campaignId) throws SQLException{
        String query ="select count(*) as 'annotationNumber' from annotation where imageId in (select imageId from imageCampaign where campaignId=?)";

        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1,campaignId);
            try (ResultSet result = statement.executeQuery();) {
                result.next();
                return result.getInt(1);
            }
        }
    }
}
