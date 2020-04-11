package it.polimi.tiw.beans;

public class Campaign {
    private int id;
    private String name;
    private String client;
    private String state;
    private String badge;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;

        if(state.equals("created")){
            this.badge = "warning";
        } else if (state.equals("started")){
            this.badge = "success";
        } else if (state.equals("closed")){
            this.badge = "danger";
        } else {
            this.badge = "secondary";
        }

    }

    public String getBadge(){
        return this.badge;
    }
}
