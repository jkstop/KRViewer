package ru.jkstop.krviewer.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import ru.jkstop.krviewer.items.Room;

/**
 * БД Помещений
 */
public class RoomsDB extends SQLiteOpenHelper implements BaseColumns{

    private static final String name = "Rooms.db";
    private static final int version = 1;

    public static final String TABLE_ROOMS = "Помещения";
    public static final String COLUMN_ROOM = "Помещение";
    public static final String COLUMN_STATUS = "Статус";
    public static final String COLUMN_ACCESS = "Доступ";
    public static final String COLUMN_OPEN_TIME = "Время";
    public static final String COLUMN_USER_NAME = "Пользователь";
    public static final String COLUMN_USER_RADIOLABEL = "Метка";
    public static final String COLUMN_USER_PHOTO_PATH = "Фото";
    private static final String CREATE_ROOMS_BASE = "CREATE TABLE " + TABLE_ROOMS + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_ROOM + " text, "
            + COLUMN_STATUS + " integer, "
            + COLUMN_ACCESS + " integer, "
            + COLUMN_OPEN_TIME + " long, "
            + COLUMN_USER_NAME + " text, "
            + COLUMN_USER_RADIOLABEL + " text, "
            + COLUMN_USER_PHOTO_PATH + " text);";

    private static final String DELETE_ROOMS_BASE = "DROP TABLE IF EXISTS " + TABLE_ROOMS;

    public RoomsDB(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ROOMS_BASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_ROOMS_BASE);
        onCreate(db);
    }

    public static void addRoom(@NonNull Room room) {
        try {
            if (isRoomAlreadyInBase(room.getName())){
                updateRoom(room);
            } else {
                ContentValues cv = new ContentValues();
                cv.put(COLUMN_ROOM, room.getName());
                cv.put(COLUMN_STATUS, room.getStatus());
                cv.put(COLUMN_ACCESS,room.getAccess());
                cv.put(COLUMN_OPEN_TIME, room.getOpenTime());
                cv.put(COLUMN_USER_NAME, room.getUserName());
                cv.put(COLUMN_USER_RADIOLABEL, room.getUserRadioLabel());
                cv.put(COLUMN_USER_PHOTO_PATH, room.getUserPhotoPath());
                DbShare.getDataBase(DbShare.ROOMS).insert(TABLE_ROOMS, null, cv);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean updateRoom(Room room){
        try {
            String roomPosition;
            if (room.getName() != null){
                roomPosition = DbShare.getDataBase(DbShare.ROOMS).compileStatement("SELECT * FROM " + TABLE_ROOMS +
                        " WHERE " + COLUMN_ROOM + " = '" + room.getName() + "'").simpleQueryForString();
            }else{
                roomPosition = DbShare.getDataBase(DbShare.ROOMS).compileStatement("SELECT * FROM " + TABLE_ROOMS +
                        " WHERE " + COLUMN_USER_RADIOLABEL + " = " + room.getUserRadioLabel()).simpleQueryForString();
            }

            if (roomPosition!=null){
                ContentValues cv = new ContentValues();
                cv.put(COLUMN_STATUS, room.getStatus());
                cv.put(COLUMN_OPEN_TIME, room.getOpenTime());
                cv.put(COLUMN_USER_NAME, room.getUserName());
                cv.put(COLUMN_ACCESS, room.getAccess());
                cv.put(COLUMN_USER_RADIOLABEL, room.getUserRadioLabel());
                cv.put(COLUMN_USER_PHOTO_PATH, room.getUserPhotoPath());

                DbShare.getDataBase(DbShare.ROOMS).update(TABLE_ROOMS,cv, _ID + "=" + roomPosition, null);
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
     return false;
    }

    public static void deleteRoom (String roomName){
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.ROOMS,
                    TABLE_ROOMS,
                    new String[]{_ID, COLUMN_ROOM},
                    COLUMN_ROOM + " =?",
                    new String[]{roomName},
                    null,
                    null,
                    "1");
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()){
                DbShare.getDataBase(DbShare.ROOMS).delete(TABLE_ROOMS,
                        _ID + "=" + cursor.getString(cursor.getColumnIndex(_ID)),
                        null);
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
    }

    public static ArrayList<Room> getRoomList(){
        Cursor cursor = null;
        ArrayList<Room> rooms = new ArrayList<>();
        try {
            cursor = DbShare.getCursor(DbShare.ROOMS,
                    TABLE_ROOMS,
                    null,null,null,null,
                    COLUMN_ROOM + " ASC",
                    null);
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                rooms.add(new Room()
                        .setName(cursor.getString(cursor.getColumnIndex(COLUMN_ROOM)))
                        .setStatus(cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS)))
                        .setAccess(cursor.getInt(cursor.getColumnIndex(COLUMN_ACCESS)))
                        .setOpenTime(cursor.getLong(cursor.getColumnIndex(COLUMN_OPEN_TIME)))
                        .setUserName(cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME)))
                        .setUserRadioLabel(cursor.getString(cursor.getColumnIndex(COLUMN_USER_RADIOLABEL)))
                        .setUserPhotoPath(cursor.getString(cursor.getColumnIndex(COLUMN_USER_PHOTO_PATH))));
            }

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return rooms;
    }

    public static ArrayList<String> getRoomsNamesList(){
        Cursor cursor = null;
        ArrayList<String> items = new ArrayList<>();
        try {

            cursor = DbShare.getCursor(DbShare.ROOMS,
                    TABLE_ROOMS,
                    new String[]{COLUMN_ROOM},
                    null,
                    null,
                    null,
                    COLUMN_ROOM + " ASC",
                    null);
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()){
                items.add(cursor.getString(cursor.getColumnIndex(COLUMN_ROOM)));
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return items;
    }

    public static boolean isRoomAlreadyInBase (String roomName){
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.ROOMS,
                    TABLE_ROOMS,
                    new String[]{COLUMN_ROOM},
                    COLUMN_ROOM + " =?",
                    new String[]{roomName},
                    null,
                    null,
                    "1");
            return cursor.getCount() > 0;
        }catch (Exception e){
            return false;
        } finally {
            closeCursor(cursor);
        }
    }

    public static int getRoomStatus(String roomName){
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.ROOMS, TABLE_ROOMS,
                    new String[]{COLUMN_STATUS},
                    COLUMN_ROOM + " =?",
                    new String[]{roomName},
                    null,
                    null,
                    "1");
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                return cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return -1;
    }

    public static long getRoomOpenTime (String roomName){
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.ROOMS, TABLE_ROOMS,
                    new String[]{COLUMN_OPEN_TIME},
                    COLUMN_ROOM + " =?",
                    new String[]{roomName},
                    null,
                    null,
                    "1");
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                return cursor.getLong(cursor.getColumnIndex(COLUMN_OPEN_TIME));
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return 0;
    }

    public static void clear(){
        try {
            DbShare.getDataBase(DbShare.ROOMS).delete(TABLE_ROOMS,null,null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void closeCursor(Cursor cursor){
        if (cursor!=null) cursor.close();
    }

}
