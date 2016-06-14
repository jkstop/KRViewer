package ru.jkstop.krviewer.databases;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.jkstop.krviewer.App;

/**
 * init and share databases
 */
public class DbShare {

    public static final int USERS = 0;
    public static final int DB_JOURNAL = 1;
    public static final int DB_ROOM = 2;

    private static UsersDB mDataBaseUsersOpenHelper;
   // private static JournalDBinit mDataBaseJournalOpenHelper;
   // private static RoomDBinit mDataBaseRoomsOpenHelper;
    private static SQLiteDatabase mDataBaseUsers, mDataBaseJournal, mDataBaseRooms;
    private static Cursor mCursor;

    public DbShare(){

        mDataBaseUsers = getDataBase(USERS);
        mDataBaseJournal = getDataBase(DB_JOURNAL);
        mDataBaseRooms = getDataBase(DB_ROOM);
    }

    public static SQLiteDatabase getDataBase(int db){
        switch (db){
            case USERS:
                if (mDataBaseUsers == null || !mDataBaseUsers.isOpen()){
                    mDataBaseUsersOpenHelper = new UsersDB(App.getAppContext());
                    mDataBaseUsers = mDataBaseUsersOpenHelper.getWritableDatabase();
                }
                return mDataBaseUsers;

          //  case DB_JOURNAL:
          //      if (mDataBaseJournal == null || !mDataBaseJournal.isOpen()){
          //          mDataBaseJournalOpenHelper = new JournalDBinit(App.getAppContext());
          //          mDataBaseJournal = mDataBaseJournalOpenHelper.getWritableDatabase();
          //      }
          //      return mDataBaseJournal;

          //  case DB_ROOM:
          ///      if (mDataBaseRooms == null || !mDataBaseRooms.isOpen()){
           //         mDataBaseRoomsOpenHelper = new RoomDBinit(App.getAppContext());
         //           mDataBaseRooms = mDataBaseRoomsOpenHelper.getWritableDatabase();
         //       }
         //       return mDataBaseRooms;

            default:
                    return null;
        }
    }

    public static Cursor getCursor(int db, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String orderBy, String limit){
        mCursor = getDataBase(db).query(table, columns, selection, selectionArgs, groupBy, null, orderBy, limit);

        return mCursor;
    }


    public static void closeDB(){

        if (mDataBaseUsersOpenHelper !=null) mDataBaseUsersOpenHelper.close();
        if (mDataBaseUsers !=null) mDataBaseUsers.close();
       // if (mDataBaseJournalOpenHelper!=null) mDataBaseJournalOpenHelper.close();
      //  if (mDataBaseJournal!=null) mDataBaseJournal.close();
      //  if (mDataBaseRoomsOpenHelper!=null) mDataBaseRoomsOpenHelper.close();
      //  if (mDataBaseRooms!=null) mDataBaseRooms.close();
      // if (mCursor !=null) mCursor.close();
    }
}
