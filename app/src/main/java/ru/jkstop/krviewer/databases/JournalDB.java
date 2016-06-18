package ru.jkstop.krviewer.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.io.File;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import ru.jkstop.krviewer.App;
import ru.jkstop.krviewer.SharedPrefs;
import ru.jkstop.krviewer.items.JournalItem;

/**
 * БД Журнала
 */
public class JournalDB extends SQLiteOpenHelper implements BaseColumns {

    public static final int COUNT_TODAY = 2;
    public static final int COUNT_TOTAL = 3;

    private static final String name = "Journal.db";
    private static final int version = 1;

    public static final String TABLE_JOURNAL = "Journal";
    public static final String COLUMN_ACCOUNT_ID = "id_аккаунта";
    public static final String COLUMN_ROOM_NAME = "Помещение";
    public static final String COLUMN_OPEN_TIME = "Вход";
    public static final String COLUMN_CLOSE_TIME = "Выход";
    public static final String COLUMN_ACCESS = "Доступ";
    public static final String COLUMN_USER_NAME = "ФИО";
    public static final String COLUMN_USER_RADIO_LABEL = "Радиометка";

    private static final String SQL_CREATE_BASE_JOURNAL = "create table " + TABLE_JOURNAL + " (" + BaseColumns._ID + " integer primary key autoincrement, "
            + COLUMN_ACCOUNT_ID + " text, "
            + COLUMN_ROOM_NAME + " text, "
            + COLUMN_OPEN_TIME + " long, "
            + COLUMN_CLOSE_TIME + " long, "
            + COLUMN_ACCESS + " integer, "
            + COLUMN_USER_NAME + " text, "
            + COLUMN_USER_RADIO_LABEL + " text);";

    private static final String SQL_DELETE_BASE_JOURNAL = "DROP TABLE IF EXISTS "
            + TABLE_JOURNAL;

    public JournalDB(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_BASE_JOURNAL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_BASE_JOURNAL);
        onCreate(db);
    }

    public static long addJournalItem(JournalItem journalItem){

        SQLiteDatabase mDataBase = DbShare.getDataBase(DbShare.JOURNAL);
        Cursor cursor = null;
        try {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_ACCOUNT_ID, SharedPrefs.getActiveAccountID());
            cv.put(COLUMN_ROOM_NAME, journalItem.getRoomName());
            cv.put(COLUMN_OPEN_TIME, journalItem.getOpenTime());
            cv.put(COLUMN_CLOSE_TIME, journalItem.getCloseTime());
            cv.put(COLUMN_ACCESS,journalItem.getAccess());
            cv.put(COLUMN_USER_NAME,journalItem.getUserName());
            cv.put(COLUMN_USER_RADIO_LABEL, journalItem.getUserRadioLabel());

            long position = mDataBase.insert(TABLE_JOURNAL, null, cv);

            cursor = DbShare.getCursor(DbShare.JOURNAL,
                    TABLE_JOURNAL,
                    new String[]{COLUMN_OPEN_TIME},
                    null, null, null, null, null);

            return position;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }finally {
            closeCursor(cursor);
        }
    }

    public static ArrayList<Long> getJournalItemsOpenTime(Date date){
        DateFormat dateFormat = DateFormat.getDateInstance();
        ArrayList <Long> items = new ArrayList<>();
        Cursor cursor = null;
        try {

            cursor = DbShare.getCursor(DbShare.JOURNAL,
                    TABLE_JOURNAL,
                    new String[]{COLUMN_OPEN_TIME},
                    COLUMN_ACCOUNT_ID + " =?",
                    new String[]{SharedPrefs.getActiveAccountID()},
                    null,
                    COLUMN_OPEN_TIME,
                    null);
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    if (date!=null){ //возвращаем тэги (они же время входа) на указанную дату
                        if (dateFormat.format(date).equals(dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_OPEN_TIME)))))){
                            items.add(cursor.getLong(cursor.getColumnIndex(COLUMN_OPEN_TIME)));
                        }
                    } else { //если дата не указана, то возвращаем все
                        items.add(cursor.getLong(cursor.getColumnIndex(COLUMN_OPEN_TIME)));
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeCursor(cursor);
        }
        return items;
    }

    public static ArrayList<JournalItem> getJournalItems (Date date){
        DateFormat dateFormat = DateFormat.getDateInstance();
        ArrayList <JournalItem> items = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.JOURNAL,
                    TABLE_JOURNAL,
                    null,
                    COLUMN_ACCOUNT_ID + " =?",
                    new String[]{SharedPrefs.getActiveAccountID()},
                    null,
                    COLUMN_OPEN_TIME,
                    null);
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    if (date!=null){ //если есть дата, то добавляем в список JournalItem с этой датой
                        if (dateFormat.format(date).equals(dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_OPEN_TIME)))))){
                            items.add(new JournalItem()
                                    .setRoomName(cursor.getString(cursor.getColumnIndex(COLUMN_ROOM_NAME)))
                                    .setAccess(cursor.getInt(cursor.getColumnIndex(COLUMN_ACCESS)))
                                    .setOpenTime(cursor.getLong(cursor.getColumnIndex(COLUMN_OPEN_TIME)))
                                    .setCloseTime(cursor.getLong(cursor.getColumnIndex(COLUMN_CLOSE_TIME)))
                                    .setUserName(cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME)))
                                    .setUserRadioLabel(cursor.getString(cursor.getColumnIndex(COLUMN_USER_RADIO_LABEL))));
                        }
                    } else { //нет даты, возвращаем все
                        items.add(new JournalItem()
                                .setRoomName(cursor.getString(cursor.getColumnIndex(COLUMN_ROOM_NAME)))
                                .setAccess(cursor.getInt(cursor.getColumnIndex(COLUMN_ACCESS)))
                                .setOpenTime(cursor.getLong(cursor.getColumnIndex(COLUMN_OPEN_TIME)))
                                .setCloseTime(cursor.getLong(cursor.getColumnIndex(COLUMN_CLOSE_TIME)))
                                .setUserName(cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME)))
                                .setUserRadioLabel(cursor.getString(cursor.getColumnIndex(COLUMN_USER_RADIO_LABEL))));
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return items;
    }

    public static ArrayList<Long> getUnclosedOpenTime(){
        Cursor cursor = null;
        ArrayList<Long> items = new ArrayList<>();
        try {
            cursor = DbShare.getCursor(DbShare.JOURNAL,
                    TABLE_JOURNAL,
                    new String[]{COLUMN_OPEN_TIME},
                    COLUMN_CLOSE_TIME + " =?",
                    new String[]{"0"},
                    null, null, null);
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()){
                items.add(cursor.getLong(cursor.getColumnIndex(COLUMN_OPEN_TIME)));
            }

        }catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return items;
    }

    public static int getCount(int type){
        int count = 0;
        DateFormat dateFormat = DateFormat.getDateInstance();
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.JOURNAL,
                    TABLE_JOURNAL,
                    new String[]{COLUMN_OPEN_TIME},
                    COLUMN_ACCOUNT_ID + " =?",
                    new String[]{SharedPrefs.getActiveAccountID()},
                    null,
                    null,
                    null);
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                if (type == COUNT_TODAY){
                    String today = dateFormat.format(new Date(System.currentTimeMillis()));
                    while (cursor.moveToNext()){
                        if (dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_OPEN_TIME))))
                                .equals(today)){
                            count++;
                        }
                    }
                } else {
                    count = cursor.getCount();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeCursor(cursor);
        }
        return count;
    }

    public static ArrayList<String> getDates(){
        DateFormat dateFormat = DateFormat.getDateInstance();
        final ArrayList <String> items = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.JOURNAL,
                    TABLE_JOURNAL,
                    new String[]{COLUMN_OPEN_TIME},
                    COLUMN_ACCOUNT_ID + " =?",
                    new String[]{SharedPrefs.getActiveAccountID()},
                    null,
                    COLUMN_OPEN_TIME + " DESC",
                    null);
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    String selectedDate = dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_OPEN_TIME))));
                    if (!items.contains(selectedDate)){
                        items.add(selectedDate);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeCursor(cursor);
        }

        return items;
    }

    public static void updateItem (Long openTime, Long closeTine){
        SQLiteDatabase mDataBase = DbShare.getDataBase(DbShare.JOURNAL);
        Cursor cursor;
        ContentValues cv = new ContentValues();
        try {
            cursor = DbShare.getCursor(DbShare.JOURNAL,
                    TABLE_JOURNAL,
                    new String[] {_ID},
                    COLUMN_OPEN_TIME + " =?",
                    new String[]{String.valueOf(openTime)},
                    null,
                    null,
                    "1");
            cursor.moveToFirst();

            cv.put(COLUMN_CLOSE_TIME, closeTine);
            mDataBase.update(TABLE_JOURNAL, cv, _ID + "=" + cursor.getInt(cursor.getColumnIndex(_ID)), null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void deleteItem (long openTime){
        SQLiteDatabase mDataBase = DbShare.getDataBase(DbShare.JOURNAL);
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.JOURNAL,
                    TABLE_JOURNAL,
                    new String[]{_ID},
                    COLUMN_OPEN_TIME + " =?",
                    new String[]{String.valueOf(openTime)},
                    null,
                    null,
                    "1");
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                mDataBase.delete(TABLE_JOURNAL, _ID + "=" + cursor.getInt(cursor.getColumnIndex(_ID)), null);
            }

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
    }

    private static void closeCursor(Cursor cursor){
        if (cursor!=null) cursor.close();

    }

    public static void clear(){
        SQLiteDatabase mDataBase = DbShare.getDataBase(DbShare.JOURNAL);
        try {
            mDataBase.delete(TABLE_JOURNAL, null, null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static  void backupJournalToXLS(){

        File file = new File(App.getAppContext().getFilesDir(),"/Journal.xls");
        Cursor cursor = null;

        WorkbookSettings workbookSettings = new WorkbookSettings();
        workbookSettings.setLocale(new Locale("ru","RU"));
        WritableWorkbook workbook;
        try {
            ArrayList<String> datesString = getDates();

            workbook = Workbook.createWorkbook(file,workbookSettings);
            DateFormat dateFormat = DateFormat.getDateInstance();

            if (datesString.size()!=0){
                cursor = DbShare.getCursor(DbShare.JOURNAL,
                        TABLE_JOURNAL,
                        new String[]{COLUMN_ACCOUNT_ID, COLUMN_ROOM_NAME, COLUMN_OPEN_TIME, COLUMN_CLOSE_TIME, COLUMN_USER_NAME},
                        COLUMN_ACCOUNT_ID + " =?",
                        new String[]{SharedPrefs.getActiveAccountID()},
                        null,
                        null,
                        null);

                for (int i=0;i<datesString.size();i++){

                    if (cursor.getCount()!=0){
                        cursor.moveToPosition(-1);
                        int row=1;

                        WritableSheet daySheet = workbook.createSheet(datesString.get(i),i);
                        daySheet.addCell(new Label(0,0,cursor.getColumnName(cursor.getColumnIndex(COLUMN_ROOM_NAME))));
                        daySheet.addCell(new Label(1,0,cursor.getColumnName(cursor.getColumnIndex(COLUMN_OPEN_TIME))));
                        daySheet.addCell(new Label(2,0,cursor.getColumnName(cursor.getColumnIndex(COLUMN_CLOSE_TIME))));
                        daySheet.addCell(new Label(3,0,cursor.getColumnName(cursor.getColumnIndex(COLUMN_USER_NAME))));

                        while (cursor.moveToNext()){
                            if (datesString.get(i).equals(dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_OPEN_TIME)))))){
                                if (SharedPrefs.getActiveAccountID().equals(cursor.getString(cursor.getColumnIndex(COLUMN_ACCOUNT_ID)))){
                                    daySheet.addCell(new Label(0,row,cursor.getString(cursor.getColumnIndex(COLUMN_ROOM_NAME))));
                                    daySheet.addCell(new Label(1,row,String.valueOf(new Time(cursor.getLong(cursor.getColumnIndex(COLUMN_OPEN_TIME))))));
                                    daySheet.addCell(new Label(2,row,String.valueOf(new Time(cursor.getLong(cursor.getColumnIndex(COLUMN_CLOSE_TIME))))));
                                    daySheet.addCell(new Label(3,row,cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME))));
                                    row++;
                                }
                            }
                        }
                    }
                }
                try {
                    workbook.write();
                    workbook.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
    }


}
