package ru.jkstop.krviewer.items;

/**
 * Помещение
 */
public class Room {

    public static final int STATUS_BUSY = 0;
    public static final int STATUS_FREE = 1;
    public static final int ACCESS_CLICK = 100;
    public static final int ACCESS_CARD = 101;

    private String name;
    private int status;
    private int access;
    private long openTime;
    private String userName;
    private String userRadioLabel;

    public Room setName (String name){
        this.name = name;
        return this;
    }

    public Room setStatus (int status){
        this.status = status;
        return this;
    }

    public Room setAccess (int access){
        this.access = access;
        return this;
    }

    public Room setOpenTime(long openTime){
        this.openTime = openTime;
        return this;
    }

    public Room setUserName (String userName){
        this.userName = userName;
        return this;
    }

    public Room setUserRadioLabel (String userRadioLabel){
        this.userRadioLabel = userRadioLabel;
        return this;
    }

    public String getName(){
        return name;
    }

    public int getStatus(){
        return status;
    }

    public int getAccess(){
        return access;
    }

    public long getOpenTime(){
        return openTime;
    }

    public String getUserName(){
        return userName;
    }

    public String getUserRadioLabel(){
        return userRadioLabel;
    }

}
