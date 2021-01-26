package yelm.io.yelm.chat.model;

import android.net.Uri;

public class ChatContent {

    private String nameSender;
    private String textSender;
    private String imageURL;
    private String imageUri;

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public ChatContent(String nameSender, String textSender, String imageURL, String imageUri) {
        this.nameSender = nameSender;
        this.textSender = textSender;
        this.imageURL = imageURL;
        this.imageUri = imageUri;
    }

    public String getNameSender() {
        return nameSender;
    }

    public void setNameSender(String nameSender) {
        this.nameSender = nameSender;
    }

    public String getTextSender() {
        return textSender;
    }

    public void setTextSender(String textSender) {
        this.textSender = textSender;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
