package bsu.fpmi.chat.model;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import java.util.UUID;

/**
 * Created by Victor on 02-May-15.
 */
public class Message implements JSONAware {
    private String username;
    private String text;
    private String time;
    private String id;
    private boolean edited;
    private boolean deleted;

    public Message() {
        username = "";
        text = "";
        time="";
        id = UUID.randomUUID().toString();
        edited = false;
        deleted = false;
    }

    public Message(String username, String text, String time,String id, boolean edited, boolean deleted) {
        this.username = username;
        this.text = text;
        this.time=time;
        this.id = UUID.randomUUID().toString();
        this.edited = edited;
        this.deleted = deleted;
    }

    public String getUsername() {return username;}
    public void setUsername(String userName){username = userName;}
    public String getText() {return text;}
    public void setText(String text){this.text = text;}
    public String getTime() {return time;}
    public void setTime(String time){this.time = time;}
    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    public boolean isEdited(){return edited;}
    public void setEdited(boolean edited){this.edited = edited;}
    public boolean isDeleted(){return deleted;}
    public void setDeleted(boolean deleted){this.deleted = deleted;}

    public boolean deleteMessage() {
        if (!deleted) {
            edited = false;
            deleted = true;
            text = "";
            return true;
        }
        return false;
    }

    public boolean editMessage(String text) {
        if (!deleted) {
            edited = true;
            this.text = text;
            return true;
        }
        return false;
    }

    @Override
    public String toJSONString() {
        JSONObject obj = new JSONObject();
        obj.put("username", username);
        obj.put("text", text);
        obj.put("time",time);
        obj.put("id", id);
        obj.put("edited", edited);
        obj.put("deleted", deleted);
        return obj.toString();
    }

    @Override
    public String toString() {
        return username + " : " + text;
    }

    @Override
    public boolean equals(Object obj) {
        return (((Message) obj).getId().equals(id));
    }
}

