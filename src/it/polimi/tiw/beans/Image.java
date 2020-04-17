package it.polimi.tiw.beans;

import it.polimi.tiw.utility.JsonSupport;

import java.util.Date;

public class Image implements JsonSupport {
    private int id;
    private Date date;
    private float latitude;
    private float longitude;
    private String resolution;
    private String source;
    private String region;
    private String town;
    private String url;

    public Image(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String convertToJSON() {
        String result ="{";
        result += "\"id\":\""+id+"\",";
        result += "\"date\":\""+date.toString()+"\",";
        result += "\"latitude\":\""+latitude+"\",";
        result += "\"longitude\":\""+longitude+"\",";
        result += "\"resolution\":\""+resolution+"\",";
        result += "\"source\":\""+source+"\",";
        result += "\"region\":\""+region+"\",";
        result += "\"town\":\""+town+"\",";
        result += "\"url\":\""+url+"\"";
        result += "}";
        return result;
    }

    public String convertToGeoJSON() {
        String result = "{";
        result += "\"type\": \"Feature\",\"properties\": {";
        result += "\"title\": Image" + id + "\"},";
        result += "\"geometry\": {";
        result += "\"coordinates\": [" + longitude + ", " + latitude + "],";
        result += "\"type\": \"Point\"}}";

        return result;
    }

}
