package it.polimi.tiw.beans;

public class Alerts {
    private boolean show;
    private boolean dismissible = false;
    private String type;
    private String content;

    public Alerts(boolean show, String type, String content) {
        this.show = show;
        this.type = type;
        this.content = content;
    }

    public void hide(){
        this.show = false;
    }

    public void show(){
        this.show = true;
    }

    public boolean isVisible(){
        return show;
    }

    public void setContent(String content){
        this.content = content;
    }

    public String getContent(){
        return this.content;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void dismiss(){
        this.dismissible = true;
    }

    public boolean isDismissible(){
        return this.dismissible;
    }
}
