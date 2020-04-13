package it.polimi.tiw.beans;

import it.polimi.tiw.utility.JsonSupport;

import java.util.Date;

public class Annotation implements JsonSupport {
    private int workerId;
    private int imageId;
    private Date date;
    private int validity;
    private String trust;
    private String note;

    public Annotation(){}

    public int getWorkerId() {
        return workerId;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getValidity() {
        return validity;
    }

    public void setValidity(int validity) {
        this.validity = validity;
    }

    public String getTrust() {
        return trust;
    }

    public void setTrust(String trust) {
        this.trust = trust;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String convertToJSON() {
        String result ="{";
        result += "\"workerId\":\""+workerId+"\",";
        result += "\"imageId\":\""+imageId+"\",";
        result += "\"date\":\""+date.toString()+"\",";
        result += "\"validity\":\""+validity+"\",";
        result += "\"trust\":\""+trust+"\",";
        result += "\"note\":\""+note+"\"";
        result += "}";
        return result;
    }

    @Override
    public void createFromJSON(String json) {

    }
}
