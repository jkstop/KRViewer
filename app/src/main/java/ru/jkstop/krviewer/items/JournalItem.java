package ru.jkstop.krviewer.items;

/**
 * Created by ivsmirnov on 16.06.2016.
 */
public class JournalItem {

    private String RoomName;
    private long OpenTime;
    private long CloseTime;
    private int Access;
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

    public JournalItem setCloseTime(Long closeTime){
        if (closeTime == null) closeTime = (long) 0;
        this.CloseTime = closeTime;
        return this;
    }

    public Long getOpenTime(){
        return OpenTime;
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
