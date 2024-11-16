package com.cmpt.memogram.classes;

public class Post {
    private String descriptionPath;
    private String imagePath;
    private String titlePath;

    public Post() {
    }

    public Post(String descriptionPath, String imagePath, String titlePath) {
        this.descriptionPath = descriptionPath;
        this.imagePath = imagePath;
        this.titlePath = titlePath;
    }

    public String getDescriptionPath() {
        return descriptionPath;
    }

    public void setDescriptionPath(String descriptionPath) {
        this.descriptionPath = descriptionPath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getTitlePath() {
        return titlePath;
    }

    public void setTitlePath(String titlePath) {
        this.titlePath = titlePath;
    }
}