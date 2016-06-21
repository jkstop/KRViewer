package ru.jkstop.krviewer.databases;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.jkstop.krviewer.items.App;

/**
 * init and share databases
 */
public class DbShare {

    public static final int USERS = 0;
    public static final int JOURNAL = 1;
    public static final int ROOMS = 2;

    private static UsersDB dbUsersOpenHelper;
    private static JournalDB dbJournalOpenHelper;
    private static RoomsDB dbRoomsOpenHelper;
    private static SQLiteDatabase dbUsers, dbJournal, dbRooms;
    private static Cursor mCursor;

    public DbShare(){

        dbUsers = getDataBase(USERS);
        dbJournal = getDataBase(JOURNAL);
        dbRooms = getDataBase(ROOMS);
    }

    public static SQLiteDatabase getDataBase(int db){
        switch (db){
            case USERS:
                if (dbUsers == null || !dbUsers.isOpen()){
                    dbUsersOpenHelper = new UsersDB(App.getAppContext());
                    dbUsers = dbUsersOpenHelper.getWritableDatabase();
                }
                return dbUsers;

            case JOURNAL:
                if (dbJournal == null || !dbJournal.isOpen()){
                    dbJournalOpenHelper = new JournalDB(App.getAppContext());
                    dbJournal = dbJournalOpenHelper.getWritableDatabase();
                }
                return dbJournal;

            case ROOMS:
                if (dbRooms == null || !dbRooms.isOpen()){
                    dbRoomsOpenHelper = new RoomsDB(App.getAppContext());
                    dbRooms = dbRoomsOpenHelper.getWritableDatabase();
                }
                return dbRooms;

            default:
                    return null;
        }
    }

    public static Cursor getCursor(int db, String table, String[] columns, String selection, String[] selectionArgs, String orderBy, String limit){
        mCursor = getDataBase(db).query(table, columns, selection, selectionArgs, null, null, orderBy, limit);

        return mCursor;
    }


    public static void closeDB(){

        if (dbUsersOpenHelper !=null) dbUsersOpenHelper.close();
        if (dbUsers !=null) dbUsers.close();
        if (dbJournalOpenHelper !=null) dbJournalOpenHelper.close();
        if (dbJournal !=null) dbJournal.close();
        if (dbRoomsOpenHelper !=null) dbRoomsOpenHelper.close();
        if (dbRooms !=null) dbRooms.close();
        if (mCursor !=null) mCursor.close();
    }
}
