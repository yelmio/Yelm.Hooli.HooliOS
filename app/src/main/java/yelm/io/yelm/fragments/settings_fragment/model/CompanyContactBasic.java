package yelm.io.yelm.fragments.settings_fragment.model;

public class CompanyContactBasic {

    String name;
    String content;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public CompanyContactBasic(String name, String content) {
        this.name = name;
        this.content = content;
    }
}
