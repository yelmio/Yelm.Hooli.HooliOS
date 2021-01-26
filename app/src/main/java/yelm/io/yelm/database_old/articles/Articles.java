package yelm.io.yelm.database_old.articles;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Articles")
public class Articles {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;


    @ColumnInfo(name = "theme")
    public String theme;


    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "subtitle")
    public String subtitle;



    @ColumnInfo(name = "image")
    public String image;

    @ColumnInfo(name = "text_about")
    public String text_about;

    @ColumnInfo(name = "viewscreen")
    public String viewscreen;



    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getViewscreen() {
        return viewscreen;
    }

    public void setViewscreen(String viewscreen) {
        this.viewscreen = viewscreen;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getText_about() {
        return text_about;
    }

    public void setText_about(String text_about) {
        this.text_about = text_about;
    }

    public Articles(String theme, String title, String subtitle, String image, String text_about, String viewscreen) {
        this.theme = theme;
        this.title = title;
        this.subtitle = subtitle;
        this.image = image;
        this.text_about = text_about;
        this.viewscreen = viewscreen;
    }
}
