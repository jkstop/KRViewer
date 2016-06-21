package ru.jkstop.krviewer.items;

/**
 * Пользователь
 */
public class User {
    private String initials;
    private String division;
    private String photoPath;
    private String radioLabel;
    private String photoBinary;

    public User setInitials (String initials){
        this.initials = initials;
        return this;
    }

    public User setDivision (String division){
        this.division = division;
        return this;
    }

    public User setPhotoPath (String photoPath){
        this.photoPath = photoPath;
        return this;
    }

    public User setRadioLabel (String radioLabel){
        this.radioLabel = radioLabel;
        return this;
    }

    public User setPhotoBinary (String photoBinary){
        this.photoBinary = photoBinary;
        return this;
    }

    public String getInitials(){
        return initials;
    }

    public String getDivision(){
        return division;
    }

    public String getPhotoPath(){
        if (photoPath == null) photoPath = "";
        return photoPath;
    }

    public String getRadioLabel(){
        return radioLabel;
    }

    public String getPhotoBinary(){
        return photoBinary;
    }
}
