package yelm.io.yelm.chat.model;

import java.util.ArrayList;

import yelm.io.yelm.main.model.Item;

public class ChatContent {

    private String from_whom;
    private String to_whom;
    private String message;
    private String created_at;
    private String type;
    private String image;
    private Item item;
    private boolean inner;

    public ChatContent(String from_whom, String to_whom, String message, String created_at, String type, String image, Item item, boolean inner) {
        this.from_whom = from_whom;
        this.to_whom = to_whom;
        this.message = message;
        this.created_at = created_at;
        this.type = type;
        this.image = image;
        this.item = item;
        this.inner = inner;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isInner() {
        return inner;
    }

    public void setInner(boolean inner) {
        this.inner = inner;
    }

    public String getFrom_whom() {
        return from_whom;
    }

    public void setFrom_whom(String from_whom) {
        this.from_whom = from_whom;
    }

    public String getTo_whom() {
        return to_whom;
    }

    public void setTo_whom(String to_whom) {
        this.to_whom = to_whom;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
