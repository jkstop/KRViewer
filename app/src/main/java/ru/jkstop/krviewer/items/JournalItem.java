package ru.jkstop.krviewer.items;

/**
 * Created by ivsmirnov on 16.06.2016.
 */
public class JournalItem {

    private String RoomName;
    private long OpenTime;
    private long CloseTime;
    private int Access;
    private String UserPhotoPath;
    private String UserName;
    private String UserRadioLabel;

    public JournalItem setUserRadioLabel(String tag){
        this.UserRadioLabel = tag;
        return this;
    }

    public String getUserRadioLabel(){
        return UserRadioLabel;
    }

    public JournalItem setUserName(String initials){
        this.UserName = initials;
        return this;
    }

    public String getUserName(){
        return UserName;
    }


    public JournalItem setUserPhotoPath(String photoPath){
        this.UserPhotoPath = photoPath;
        return this;
    }

    public String getUserPhotoPath(){
        if (UserPhotoPath == null) UserPhotoPath = "";
        return UserPhotoPath;
    }


    public JournalItem setRoomName(String roomName){
        this.RoomName = roomName;
        return this;
    }

    public String getRoomName(){
        return RoomName;
    }

    public JournalItem setOpenTime(Long openTime){
        if (openTime == null) openTime = (long) 0;
        this.OpenTime = openTime;
        return this;
    }

    public JournalItem setTimeIn (String timeIn){
        long time;
        try {
            time = Long.parseLong(timeIn);
        } catch (NumberFormatException e){
            time = (long) 0;
        }
        this.OpenTime = time;
        return this;
    }

    public JournalItem setCloseTime(String closeTime){
        long time;
        try {
            time = Long.parseLong(closeTime);
        } catch (NumberFormatException e){
            time = (long) 0;
        }
        this.CloseTime = time;
        return this;
    }

    public Long getOpenTime(){
        return OpenTime;
    }

    public JournalItem setTimeOut (Long timeOut){
        if (timeOut == null) timeOut = (long)0;
        this.CloseTime = timeOut;
        return this;
    }

    public Long getCloseTime(){
        return CloseTime;
    }

    public JournalItem setAccess(int access){
        this.Access = access;
        return this;
    }

    public int getAccess(){
        return Access;
    }
}
