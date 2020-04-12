package it.polimi.tiw.beans;

public class Campaign {
    public static final String CREATED = "created";
    public static final String STARTED = "started";
    public static final String CLOSED = "closed";


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

        if(state.equals(Campaign.CREATED)){
            this.badge = "warning";
        } else if (state.equals(Campaign.STARTED)){
            this.badge = "success";
        } else if (state.equals(Campaign.CLOSED)){
            this.badge = "danger";
        } else {
            this.badge = "secondary";
        }

    }

    public String getBadge(){
        return this.badge;
    }
}
