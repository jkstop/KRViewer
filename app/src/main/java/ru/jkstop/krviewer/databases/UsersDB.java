package ru.jkstop.krviewer.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import ru.jkstop.krviewer.App;
import ru.jkstop.krviewer.R;
import ru.jkstop.krviewer.SharedPrefs;
import ru.jkstop.krviewer.items.ImageSaver;
import ru.jkstop.krviewer.items.User;

/**
 * БД пользователей
 */
public class UsersDB extends SQLiteOpenHelper implements BaseColumns {

    private static final String name = "Users.db";
    private static final int version = 2;

    public static final String TABLE_USERS = "Пользователи";
    public static final String COLUMN_ACCOUNT_UD = "ID_Пользователя";
    public static final String COLUMN_INITIALS = "ФИО";
    public static final String COLUMN_DIVISION = "Подразделение";
    public static final String COLUMN_RADIO_LABEL = "Радиометка";
    public static final String COLUMN_PHOTO_PATH = "Фото";

    private static final String SQL_CREATE_USERS_BASE = "create table " + TABLE_USERS + " (" + BaseColumns._ID + " integer primary key autoincrement, "
            + COLUMN_ACCOUNT_UD + " text, "
            + COLUMN_INITIALS + " text, "
            + COLUMN_DIVISION + " text, "
            + COLUMN_RADIO_LABEL + " text, "
            + COLUMN_PHOTO_PATH + " text);";

    private static final String SQL_DELETE_USERS_BASE = "DROP TABLE IF EXISTS "
            + TABLE_USERS;

    public static final int SHORT_INITIALS = 10;
    public static final int FULL_INITIALS = 11;

    public UsersDB(Context context) {
        super(context,name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS_BASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_USERS_BASE);
        onCreate(db);
        //update transaction
    }

    public static User getUser (String tag){
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.USERS,
                    TABLE_USERS,
                    null,
                    COLUMN_RADIO_LABEL + " =?",
                    new String[]{tag},
                    null,
                    null,
                    "1");

            if (cursor!=null && cursor.getCount()>0){ //пользователь найден, возвращаем его
                cursor.moveToFirst();
                return new User()
                        .setInitials(cursor.getString(cursor.getColumnIndex(COLUMN_INITIALS)))
                        .setDivision(cursor.getString(cursor.getColumnIndex(COLUMN_DIVISION)))
                        .setRadioLabel(tag)
                        .setPhotoPath(cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO_PATH)));

            } else { //пользователя нет
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        } finally {
            closeCursor(cursor);
        }
    }

    public static ArrayList<User> getUserList(){
        ArrayList <User> items = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.USERS,
                    TABLE_USERS,
                    null,
                    null,
                    null,
                    null,
                    COLUMN_INITIALS + " ASC",
                    null);

            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    items.add(new User()
                            .setInitials(cursor.getString(cursor.getColumnIndex(COLUMN_INITIALS)))
                            .setDivision(cursor.getString(cursor.getColumnIndex(COLUMN_DIVISION)))
                            .setRadioLabel(cursor.getString(cursor.getColumnIndex(COLUMN_RADIO_LABEL)))
                            .setPhotoPath(cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO_PATH))));
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return items;
    }

    public static ArrayList<String> getUsersRadioLabels(){
        ArrayList <String> items = new ArrayList <>();
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.USERS,
                    TABLE_USERS,
                    new String[]{COLUMN_RADIO_LABEL},
                    null,null,null,null,null);
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()){
                items.add(cursor.getString(cursor.getColumnIndex(COLUMN_RADIO_LABEL)));
            }
            return items;
        } catch (Exception e){
            e.printStackTrace();
            return items;
        } finally {
            closeCursor(cursor);
        }
    }

    public static boolean addUser (@NonNull User user) {
        try {
            //если пользователь уже есть в базе, то обновляем запись
            if (isUserInBase(user.getRadioLabel())){
                updateUser(user);
            } else {
                if (user.getPhotoBinary() == null) user.setPhotoBinary(getBinaryDefaultPhoto());

                //сохраняем фото в память
                String photoPath = new ImageSaver(App.getAppContext())
                        .setFileName(user.getRadioLabel())
                        .save(user.getPhotoBinary(), null);
                user.setPhotoPath(photoPath);

                ContentValues cv = new ContentValues();
                cv.put(COLUMN_ACCOUNT_UD, SharedPrefs.getActiveAccountID());
                cv.put(COLUMN_INITIALS, user.getInitials());
                cv.put(COLUMN_DIVISION, user.getDivision());
                cv.put(COLUMN_RADIO_LABEL, user.getRadioLabel());
                cv.put(COLUMN_PHOTO_PATH, user.getPhotoPath());

                //пишем в базу
                DbShare.getDataBase(DbShare.USERS).insert(TABLE_USERS, null, cv);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static void updateUser(@NonNull User user){
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.USERS,
                    TABLE_USERS,
                    null,
                    COLUMN_RADIO_LABEL + " =?",
                    new String[]{user.getRadioLabel()},
                    null,
                    null,
                    null);
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                if (user.getPhotoBinary() == null) user.setPhotoBinary(getBinaryDefaultPhoto());

                //сохраняем фото в память
                String photoPath = new ImageSaver(App.getAppContext())
                        .setFileName(user.getRadioLabel())
                        .save(user.getPhotoBinary(), null);
                user.setPhotoPath(photoPath);

                ContentValues cv = new ContentValues();
                cv.put(COLUMN_INITIALS, user.getInitials());
                cv.put(COLUMN_DIVISION, user.getDivision());
                cv.put(COLUMN_RADIO_LABEL, user.getRadioLabel());
                cv.put(COLUMN_PHOTO_PATH, user.getPhotoPath());

                DbShare.getDataBase(DbShare.USERS).update(
                        TABLE_USERS,
                        cv,
                        _ID + "=" + cursor.getInt(cursor.getColumnIndex(_ID)),
                        null);

            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
    }

    public static File getUserPhoto (@NonNull String userRadioLabel){
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.USERS,
                        TABLE_USERS,
                        new String[]{COLUMN_PHOTO_PATH},
                        COLUMN_RADIO_LABEL + " =?",
                        new String[]{userRadioLabel},
                        null,
                        null,
                        "1");

                if (cursor.getCount()>0){
                    cursor.moveToFirst();
                    return new File(cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO_PATH)));
                }
            }catch (Exception e){
                e.printStackTrace();
            } finally {
                closeCursor(cursor);
            }

        return null;
    }

    //удаление пользователя
    public static void deleteUser(String userRadioLabel){

        Cursor cursor = null;
        try{
            //удаляем фото из хранилища
            getUserPhoto(userRadioLabel).delete();

            cursor = DbShare.getCursor(DbShare.USERS,
                    TABLE_USERS,
                    new String[]{_ID},
                    COLUMN_RADIO_LABEL + " =?",
                    new String[]{userRadioLabel},
                    null,
                    null,
                    null);

            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    DbShare.getDataBase(DbShare.USERS).delete(TABLE_USERS, _ID + "=" + cursor.getInt(cursor.getColumnIndex(_ID)), null);
                }
            }


        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
    }

    public static boolean isUserInBase(String radioLabel){
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.USERS,
                    TABLE_USERS,
                    new String[]{COLUMN_RADIO_LABEL},
                    COLUMN_RADIO_LABEL + " =?",
                    new String[]{radioLabel},
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

    public static String getBinaryDefaultPhoto(){
        try {
            Bitmap bitmap = BitmapFactory.decodeResource(App.getAppContext().getResources(), R.drawable.ic_user_not_found);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP,100,byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray,Base64.NO_WRAP);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static void closeCursor(Cursor cursor){
        if (cursor!=null) cursor.close();
    }

    public static void clear(){
        try {
            File filesDir = App.getAppContext().getFilesDir();
            if (filesDir.isDirectory()){
                File[]files = filesDir.listFiles();
                for (File file : files) {
                    file.delete();
                }
            }
            DbShare.getDataBase(DbShare.USERS).delete(TABLE_USERS, null, null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String createUserInitials (int initialsType, String lastname, String firstname, String midname){
        String initials = "";
        switch (initialsType){
            case SHORT_INITIALS:
                if (lastname != null && firstname != null && midname != null && firstname.length() != 0 && midname.length() != 0) {
                    initials = lastname + " " + firstname.charAt(0) + "." + midname.charAt(0) + ".";
                } else {
                    if (lastname != null && firstname != null) {
                        initials = lastname + " " + firstname;
                    } else {
                        if (lastname != null) {
                            initials = lastname;
                        }
                    }
                }
                break;
            case FULL_INITIALS:
                if (lastname != null && firstname != null && midname != null) {
                    initials = lastname + " " + firstname + " " + midname;
                } else {
                    if (lastname != null && firstname != null) {
                        initials = lastname + " " + firstname;
                    } else {
                        if (lastname != null) {
                            initials = lastname;
                        }
                    }
                }
                break;
            default:
                break;
        }
        return initials;

    }
}
