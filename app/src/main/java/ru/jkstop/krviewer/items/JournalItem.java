package ru.jkstop.krviewer.items;

/**
 * Запись журнала
 */
public class JournalItem {

    private String roomName;
    private long openTime;
    private long closeTime;
    private int access;
    private String userName;
    private String userRadioLabel;

    public JournalItem setUserRadioLabel(String tag){
        this.userRadioLabel = tag;
        return this;
    }

    public String getUserRadioLabel(){
        return userRadioLabel;
    }

    public JournalItem setUserName(String initials){
        this.userName = initials;
        return this;
    }

    public String getUserName(){
        return userName;
    }


    public JournalItem setRoomName(String roomName){
        this.roomName = roomName;
        return this;
    }

    public String getRoomName(){
        return roomName;
    }

    public JournalItem setOpenTime(Long openTime){
        if (openTime == null) openTime = (long) 0;
        this.openTime = openTime;
        return this;

    }

    public JournalItem setCloseTime(Long closeTime){
        if (closeTime == null) closeTime = (long) 0;
        this.closeTime = closeTime;
        return this;
    }

    public Long getOpenTime(){
        return openTime;
    }

    public Long getCloseTime(){
        return closeTime;
    }

    public JournalItem setAccess(int access){
        this.access = access;
        return this;
    }

    public int getAccess(){
        return access;
    }
}
