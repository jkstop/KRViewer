package ru.jkstop.krviewer.items;

/**
 * Пользователь
 */
public class User {
    private String Initials;
    private String Division;
    private String PhotoPath;
    private String RadioLabel;
    private String PhotoBinary;

    public User setInitials (String initials){
        this.Initials = initials;
        return this;
    }

    public User setDivision (String division){
        this.Division = division;
        return this;
    }

    public User setPhotoPath (String photoPath){
        this.PhotoPath = photoPath;
        return this;
    }

    public User setRadioLabel (String radioLabel){
        this.RadioLabel = radioLabel;
        return this;
    }

    public User setPhotoBinary (String photoBinary){
        this.PhotoBinary = photoBinary;
        return this;
    }

    public String getInitials(){
        return Initials;
    }

    public String getDivision(){
        return Division;
    }

    public String getPhotoPath(){
        if (PhotoPath == null) PhotoPath = "";
        return PhotoPath;
    }

    public String getRadioLabel(){
        return RadioLabel;
    }

    public String getPhotoBinary(){
        return PhotoBinary;
    }
}
