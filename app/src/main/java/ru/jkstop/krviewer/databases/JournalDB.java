package ru.jkstop.krviewer.databases;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Environment;
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
import ru.jkstop.krviewer.R;
import ru.jkstop.krviewer.SharedPrefs;
import ru.jkstop.krviewer.items.App;
import ru.jkstop.krviewer.items.JournalItem;

/**
 * БД Журнала
 */
public class JournalDB extends SQLiteOpenHelper implements BaseColumns {

    private static final String name = "Journal.db";
    private static final int version = 1;

    private static final String TABLE_JOURNAL = "Journal";
    private static final String COLUMN_ACCOUNT_ID = "id_аккаунта";
    private static final String COLUMN_ROOM_NAME = "Помещение";
    private static final String COLUMN_OPEN_TIME = "Вход";
    private static final String COLUMN_CLOSE_TIME = "Выход";
    private static final String COLUMN_ACCESS = "Доступ";
    private static final String COLUMN_USER_NAME = "ФИО";
    private static final String COLUMN_USER_RADIO_LABEL = "Радиометка";

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

    public static void addJournalItem(JournalItem journalItem){

        SQLiteDatabase dataBase = DbShare.getDataBase(DbShare.JOURNAL);
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

            dataBase.insert(TABLE_JOURNAL, null, cv);

            cursor = DbShare.getCursor(DbShare.JOURNAL,
                    TABLE_JOURNAL,
                    new String[]{COLUMN_OPEN_TIME},
                    null, null, null, null);

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeCursor(cursor);
        }
    }

    public static ArrayList<Long> getJournalItemsOpenTime(){
        ArrayList <Long> items = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.JOURNAL,
                    TABLE_JOURNAL,
                    new String[]{COLUMN_OPEN_TIME},
                    COLUMN_ACCOUNT_ID + " =?",
                    new String[]{SharedPrefs.getActiveAccountID()},
                    COLUMN_OPEN_TIME + " DESC",
                    null);
            if (cursor.getCount()>0){
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()){
                    items.add(cursor.getLong(cursor.getColumnIndex(COLUMN_OPEN_TIME)));
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
                    COLUMN_OPEN_TIME + " DESC",
                    null);
            if (cursor.getCount()>0){
                //если дата не указана, то используем последнюю известную
                cursor.moveToFirst();
                if (date == null) date = new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_OPEN_TIME)));

                cursor.moveToPosition(-1);

                while (cursor.moveToNext()){
                    if (dateFormat.format(date).equals(dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_OPEN_TIME)))))){
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
                    null, null);
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
        SQLiteDatabase dataBase = DbShare.getDataBase(DbShare.JOURNAL);
        Cursor cursor;
        ContentValues cv = new ContentValues();
        try {
            cursor = DbShare.getCursor(DbShare.JOURNAL,
                    TABLE_JOURNAL,
                    new String[] {_ID},
                    COLUMN_OPEN_TIME + " =?",
                    new String[]{String.valueOf(openTime)},
                    null,
                    "1");
            cursor.moveToFirst();

            cv.put(COLUMN_CLOSE_TIME, closeTine);
            dataBase.update(TABLE_JOURNAL, cv, _ID + "=" + cursor.getInt(cursor.getColumnIndex(_ID)), null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void deleteItem (long openTime){
        SQLiteDatabase dataBase = DbShare.getDataBase(DbShare.JOURNAL);
        Cursor cursor = null;
        try {
            cursor = DbShare.getCursor(DbShare.JOURNAL,
                    TABLE_JOURNAL,
                    new String[]{_ID},
                    COLUMN_OPEN_TIME + " =?",
                    new String[]{String.valueOf(openTime)},
                    null,
                    "1");
            if (cursor.getCount()>0){
                cursor.moveToFirst();
                dataBase.delete(TABLE_JOURNAL, _ID + "=" + cursor.getInt(cursor.getColumnIndex(_ID)), null);
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
        SQLiteDatabase dataBase = DbShare.getDataBase(DbShare.JOURNAL);
        try {
            dataBase.delete(TABLE_JOURNAL, null, null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static class backupJournalToFile extends AsyncTask<Void,Void,Void>{

        private ProgressDialog progressDialog;
        private Context context;
        private Callback callback;

        private File outputFile;

        public backupJournalToFile(Context context, Callback callback){
            this.context = context;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(App.getAppContext().getString(R.string.send_mail_file_creating));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            outputFile = new File(Environment.getExternalStorageDirectory(),"/Journal.xls");
            Cursor cursor = null;

            WorkbookSettings workbookSettings = new WorkbookSettings();
            workbookSettings.setLocale(new Locale("ru","RU"));
            WritableWorkbook workbook;
            try {
                ArrayList<String> datesString = getDates();

                workbook = Workbook.createWorkbook(outputFile, workbookSettings);
                DateFormat dateFormat = DateFormat.getDateInstance();

                if (datesString.size()!=0){
                    cursor = DbShare.getCursor(DbShare.JOURNAL,
                            TABLE_JOURNAL,
                            new String[]{COLUMN_ACCOUNT_ID, COLUMN_ROOM_NAME, COLUMN_OPEN_TIME, COLUMN_CLOSE_TIME, COLUMN_USER_NAME},
                            COLUMN_ACCOUNT_ID + " =?",
                            new String[]{SharedPrefs.getActiveAccountID()},
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
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing()){
                progressDialog.cancel();
            }

            callback.onFileCreated(outputFile);


        }

        public interface Callback{
            void onFileCreated(File file);
        }
    }

}
