package ru.jkstop.krviewer.items;

/**
 * Created by ivsmirnov on 16.06.2016.
 */
public class Room {

    public static final int STATUS_BUSY = 0;
    public static final int STATUS_FREE = 1;
    public static final int ACCESS_CLICK = 100;
    public static final int ACCESS_CARD = 101;

    private String Name;
    private int Status;
    private int Access;
    private long OpenTime;
    private String UserName;
    private String UserRadioLabel;

    public Room setName (String name){
        this.Name = name;
        return this;
    }

    public Room setStatus (int status){
        this.Status = status;
        return this;
    }

    public Room setAccess (int access){
        this.Access = access;
        return this;
    }

    public Room setOpenTime(long openTime){
        this.OpenTime = openTime;
        return this;
    }

    public Room setUserName (String userName){
        this.UserName = userName;
        return this;
    }

    public Room setUserRadioLabel (String userRadioLabel){
        this.UserRadioLabel = userRadioLabel;
        return this;
    }

    public String getName(){
        return Name;
    }

    public int getStatus(){
        return Status;
    }

    public int getAccess(){
        return Access;
    }

    public long getOpenTime(){
        return OpenTime;
    }

    public String getUserName(){
        return UserName;
    }

    public String getUserRadioLabel(){
        return UserRadioLabel;
    }

}
