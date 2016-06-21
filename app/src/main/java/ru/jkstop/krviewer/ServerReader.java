package ru.jkstop.krviewer;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import ru.jkstop.krviewer.databases.JournalDB;
import ru.jkstop.krviewer.databases.RoomsDB;
import ru.jkstop.krviewer.databases.UsersDB;
import ru.jkstop.krviewer.items.App;
import ru.jkstop.krviewer.items.JournalItem;
import ru.jkstop.krviewer.items.Room;
import ru.jkstop.krviewer.items.User;

/**
 * Чтение с сервера
 */
class ServerReader extends AsyncTask<Connection,Integer,Exception> {


    private Handler handler;
    private static final int HANDLER_UPDATE_JOURNAL = 100;
    private static final int HANDLER_UPDATE_PERSONS = 101;
    private static final int HANDLER_UPDATE_ROOMS = 102;
    private static final int HANDLER_SHOW_DIALOG = 103;


    private ProgressDialog progressDialog;
    private Callback callback;

    private int dialogMaxCount = 0;

    public ServerReader(Context context, Callback callback){
        progressDialog = new ProgressDialog(context);
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (progressDialog !=null){
                    switch (msg.what){
                        case HANDLER_SHOW_DIALOG:
                            progressDialog.show();
                            break;
                        case HANDLER_UPDATE_JOURNAL:
                            progressDialog.setMessage(App.getAppContext().getString(R.string.server_read_journal));
                            break;
                        case HANDLER_UPDATE_PERSONS:
                            progressDialog.setMessage(App.getAppContext().getString(R.string.server_read_users));
                            break;
                        case HANDLER_UPDATE_ROOMS:
                            progressDialog.setMessage(App.getAppContext().getString(R.string.server_read_rooms));
                            break;
                        default:
                            break;
                    }
                    progressDialog.setMax(dialogMaxCount);
                }
            }
        };

        if (progressDialog !=null){
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(App.getAppContext().getString(R.string.server_read_loading));
            progressDialog.setCancelable(false);
            progressDialog.setMax(0);
        }

    }

    @Override
    protected Exception doInBackground(Connection... params) {
        try {
            Connection connection = params[0];
            Statement mStatement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet mResult;
            ResultSet journalItemResult;

            /* Синхронизация журнала
                  *
                  * Пишем в устройство отсутстующие записи, лишние удаляем
                  *
              */

            //получаем список тэгов всех записей журнала на устройстве
            ArrayList<Long> mJournalTags = new ArrayList<>();
            mJournalTags.addAll(JournalDB.getJournalItemsOpenTime());

            //для каждой записи проверяем, не удалилась ли она с сервера. Коряво, но пока так.

            ResultSet journalExistResult = mStatement.executeQuery("SELECT " + ServerConnect.COLUMN_JOURNAL_TIME_IN
                    + " FROM " +ServerConnect.JOURNAL_TABLE
                    + " WHERE " + ServerConnect.COLUMN_JOURNAL_ACCOUNT_ID + " = '" + SharedPrefs.getActiveAccountID() + "'");
            journalExistResult.beforeFirst();
            if (journalExistResult.getRow()!=-1){
                while (journalExistResult.next()){
                    if (mJournalTags.contains(journalExistResult.getLong(ServerConnect.COLUMN_JOURNAL_TIME_IN))){
                        mJournalTags.remove(journalExistResult.getLong(ServerConnect.COLUMN_JOURNAL_TIME_IN));
                    }
                }
            }

            for (Long openTime : mJournalTags){
                JournalDB.deleteItem(openTime);
            }

            mJournalTags.clear();
            mJournalTags.addAll(JournalDB.getJournalItemsOpenTime());

            //получаем счетчик всех отсутствующих записей и ставим значение как максимум
            ResultSet getJournalTagsResult = connection.prepareStatement("SELECT " + ServerConnect.COLUMN_JOURNAL_TIME_IN
                    + " FROM " + ServerConnect.JOURNAL_TABLE
                    + " WHERE " + ServerConnect.COLUMN_JOURNAL_ACCOUNT_ID + " = '" + SharedPrefs.getActiveAccountID() + "'"
                    + " AND " + ServerConnect.COLUMN_JOURNAL_TIME_IN + " NOT IN (" + getInClause(mJournalTags) + ")"
                    , ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
                    .executeQuery();
            getJournalTagsResult.last();

            if (getJournalTagsResult.getRow() > 20) {

                handler.sendEmptyMessage(HANDLER_SHOW_DIALOG);
                dialogMaxCount = getJournalTagsResult.getRow();
                handler.sendEmptyMessage(HANDLER_UPDATE_JOURNAL);
            }

            //двигаем в начало
            getJournalTagsResult.beforeFirst();


            while (getJournalTagsResult.next()){
                //для каждого тэга получаем journalItem и пишем в устройство. Можно бы получить все сразу, но тогда зависает, данных много.
                journalItemResult = mStatement.executeQuery("SELECT * FROM " +ServerConnect.JOURNAL_TABLE
                        + " WHERE " + ServerConnect.COLUMN_JOURNAL_TIME_IN + " = " + getJournalTagsResult.getLong(ServerConnect.COLUMN_JOURNAL_TIME_IN));

                journalItemResult.first();
                if (journalItemResult.getRow() != 0){
                    //пишем в журнал
                    JournalDB.addJournalItem(new JournalItem()
                            .setRoomName(journalItemResult.getString(ServerConnect.COLUMN_JOURNAL_ROOM))
                            .setOpenTime(journalItemResult.getLong(ServerConnect.COLUMN_JOURNAL_TIME_IN))
                            .setCloseTime(journalItemResult.getLong(ServerConnect.COLUMN_JOURNAL_TIME_OUT))
                            .setAccess(journalItemResult.getInt(ServerConnect.COLUMN_JOURNAL_ACCESS))
                            .setUserName(journalItemResult.getString(ServerConnect.COLUMN_JOURNAL_PERSON_INITIALS))
                            .setUserRadioLabel(journalItemResult.getString(ServerConnect.COLUMN_JOURNAL_PERSON_TAG)));
                }

                publishProgress(getJournalTagsResult.getRow());
            }


            //проверяем открытые помещения. Если на сервере они закрыты, то обновляем локальный журнал.
            //сначала получаем тэги открытых помещений  в журнале (они же время входа)
            ArrayList<Long> mOpenTags = JournalDB.getUnclosedOpenTime();

            //для каждого открытого помещения проверяем, закрылось ли на сервере
            if (!mOpenTags.isEmpty()){
                mResult = mStatement.executeQuery("SELECT " + ServerConnect.COLUMN_JOURNAL_TIME_IN + ","
                        + ServerConnect.COLUMN_JOURNAL_TIME_OUT + " FROM " + ServerConnect.JOURNAL_TABLE
                        + " WHERE " + ServerConnect.COLUMN_JOURNAL_ACCOUNT_ID + " = '" + SharedPrefs.getActiveAccountID() + "'"
                        + " AND " + ServerConnect.COLUMN_JOURNAL_TIME_IN + " IN (" + getInClause(mOpenTags) + ")");
                long timeOut;
                long timeIn;
                mResult.beforeFirst();
                if (mResult.getRow()!=-1){
                    while (mResult.next()){
                        timeOut = mResult.getLong(ServerConnect.COLUMN_JOURNAL_TIME_OUT);
                        timeIn = mResult.getLong(ServerConnect.COLUMN_JOURNAL_TIME_IN);
                        if (timeOut!=0) JournalDB.updateItem(timeIn, timeOut);
                    }
                }
            }

                    /*
                    *
                    * Синхронизация пользователей
                    *
                     */

            //список тэгов всех пользователей
            ArrayList<String> mPersonsTags = new ArrayList<>();
            mPersonsTags.addAll(UsersDB.getUsersRadioLabels());

            ResultSet personItemResult;

            ResultSet personsExistResult = mStatement.executeQuery("SELECT " + ServerConnect.COLUMN_PERSONS_TAG
                    + " FROM " +ServerConnect.PERSONS_TABLE
                    + " WHERE " + ServerConnect.COLUMN_PERSONS_ACCOUNT_ID + " = '" + SharedPrefs.getActiveAccountID() + "'");
            personsExistResult.beforeFirst();
            if (personsExistResult.getRow()!=-1){
                while (personsExistResult.next()){
                    try {
                        mPersonsTags.remove(personsExistResult.getString(ServerConnect.COLUMN_PERSONS_TAG));
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            for (String userRadioLabel : mPersonsTags){
                UsersDB.deleteUser(userRadioLabel);
            }

            mPersonsTags.clear();
            mPersonsTags.addAll(UsersDB.getUsersRadioLabels());

            //выбираем записи, которые есть на сервере, но нет в устройстве. пишем в устройство отсутствующие
            ResultSet getPesonsTagsResult = connection.prepareStatement("SELECT " + ServerConnect.COLUMN_PERSONS_TAG + " FROM " + ServerConnect.PERSONS_TABLE
                    + " WHERE " + ServerConnect.COLUMN_PERSONS_ACCOUNT_ID + " = '" + SharedPrefs.getActiveAccountID() + "'"
                    + " AND "+ ServerConnect.COLUMN_PERSONS_TAG + " NOT IN (" + getInClause(mPersonsTags) + ")"
                    ,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeQuery();

            getPesonsTagsResult.last();

            if (getPesonsTagsResult.getRow() > 3){
                if (!progressDialog.isShowing()){
                    handler.sendEmptyMessage(HANDLER_SHOW_DIALOG);
                }
                dialogMaxCount = getPesonsTagsResult.getRow();
                handler.sendEmptyMessage(HANDLER_UPDATE_PERSONS);
            }

            //в начало
            getPesonsTagsResult.beforeFirst();

            while (getPesonsTagsResult.next()){
                personItemResult = mStatement.executeQuery("SELECT * FROM " +ServerConnect.PERSONS_TABLE
                        + " WHERE " + ServerConnect.COLUMN_PERSONS_ACCOUNT_ID + " = '" + SharedPrefs.getActiveAccountID() + "'"
                        + " AND " + ServerConnect.COLUMN_PERSONS_TAG
                        + " = '" + getPesonsTagsResult.getString(ServerConnect.COLUMN_PERSONS_TAG) + "'");
                personItemResult.first();
                if (personItemResult.getRow() != 0){
                    UsersDB.addUser(new User()
                            .setInitials(UsersDB.createUserInitials(
                                    personItemResult.getString(ServerConnect.COLUMN_PERSONS_LASTNAME),
                                    personItemResult.getString(ServerConnect.COLUMN_PERSONS_FIRSTNAME),
                                    personItemResult.getString(ServerConnect.COLUMN_PERSONS_MIDNAME)))
                            .setDivision(personItemResult.getString(ServerConnect.COLUMN_PERSONS_DIVISION))
                            .setRadioLabel(personItemResult.getString(ServerConnect.COLUMN_PERSONS_TAG))
                            .setPhotoBinary(personItemResult.getString(ServerConnect.COLUMN_PERSONS_PHOTO_BASE64)));
                }

                publishProgress(getPesonsTagsResult.getRow());
            }

                    /* Синхронизация помещений
                    *
                    * Пишем в устройство отсутствующие, лишние удаляем
                    *
                     */

            //выбираем записи, которые есть на сервере, но нет в устройстве. пишем в устройство отсутствующие
            ArrayList<Room> roomList = RoomsDB.getRoomList();
            ArrayList<String> roomNameList = new ArrayList<>();

            ResultSet roomExistResult;

            for (Room room : roomList){
                roomExistResult = mStatement.executeQuery("SELECT " + ServerConnect.COLUMN_ROOMS_ROOM + " FROM " +ServerConnect.ROOMS_TABLE
                        + " WHERE " + ServerConnect.COLUMN_ROOM_ACCOUNT_ID + " = '" + SharedPrefs.getActiveAccountID() + "'"
                        + " AND "+ ServerConnect.COLUMN_ROOMS_ROOM + " = '" + room.getName() + "'");
                roomExistResult.first();
                if (roomExistResult.getRow() == 0){ //помещения на сервере нет, удаляем с устройства
                    RoomsDB.deleteRoom(room.getName());
                } else { //помещение есть, добавляем в список
                    roomNameList.add(room.getName());
                }
            }

            ResultSet getRoomsItemsResult = connection.prepareStatement("SELECT * FROM " + ServerConnect.ROOMS_TABLE
                            + " WHERE " + ServerConnect.COLUMN_ROOM_ACCOUNT_ID + " = '" + SharedPrefs.getActiveAccountID() + "'"
                            + " AND "+ ServerConnect.COLUMN_ROOMS_ROOM + " NOT IN (" + getInClause(roomNameList) + ")"
                    ,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE).executeQuery();

            getRoomsItemsResult.last();

            if (getRoomsItemsResult.getRow() > 30){
                if (!progressDialog.isShowing()){
                    handler.sendEmptyMessage(HANDLER_SHOW_DIALOG);
                }
                dialogMaxCount = getRoomsItemsResult.getRow();
                handler.sendEmptyMessage(HANDLER_UPDATE_ROOMS);
            }

            //в начало
            getRoomsItemsResult.beforeFirst();

            while (getRoomsItemsResult.next()){
                RoomsDB.addRoom(new Room()
                        .setName(getRoomsItemsResult.getString(ServerConnect.COLUMN_ROOMS_ROOM))
                        .setAccess(getRoomsItemsResult.getInt(ServerConnect.COLUMN_ROOMS_ACCESS))
                        .setStatus(getRoomsItemsResult.getInt(ServerConnect.COLUMN_ROOMS_STATUS))
                        .setOpenTime(getRoomsItemsResult.getLong(ServerConnect.COLUMN_ROOMS_TIME))
                        .setUserName(getRoomsItemsResult.getString(ServerConnect.COLUMN_ROOMS_LAST_VISITER))
                        .setUserRadioLabel(getRoomsItemsResult.getString(ServerConnect.COLUMN_ROOMS_RADIO_LABEL)));
            }

            //для всех записей проверяем статус. Если на устройстве не совпадает с сервером, пишем в устройство новый статус
            //сначала получаем с сервера список всех помещений и их статусы
            mResult = mStatement.executeQuery("SELECT * FROM " + ServerConnect.ROOMS_TABLE + " WHERE " + ServerConnect.COLUMN_ROOM_ACCOUNT_ID + " = '" + SharedPrefs.getActiveAccountID() + "'");
            //для каждого значения сравниваем статус с локальным
            String aud;
            int status;
            long timeServer;
            long timeLocal;
            while (mResult.next()){
                aud = mResult.getString(ServerConnect.COLUMN_ROOMS_ROOM);
                status = mResult.getInt(ServerConnect.COLUMN_ROOMS_STATUS);
                //статус не совпал, пишем в устройство новый статус
                if (status != RoomsDB.getRoomStatus(aud)){
                    switch (status){
                        case Room.STATUS_FREE: //освобождаем помещение
                            RoomsDB.updateRoom(new Room()
                                    .setName(aud)
                                    .setStatus(status));
                            break;
                        case Room.STATUS_BUSY: //занимаем помещение
                            RoomsDB.updateRoom(new Room()
                                    .setName(aud)
                                    .setStatus(status)
                                    .setUserRadioLabel(mResult.getString(ServerConnect.COLUMN_ROOMS_RADIO_LABEL))
                                    .setUserName(mResult.getString(ServerConnect.COLUMN_ROOMS_LAST_VISITER))
                                    .setOpenTime(mResult.getLong(ServerConnect.COLUMN_ROOMS_TIME)));
                            break;
                        default:
                            break;
                    }
                } else { //статус совпал, проверяем время входа
                    timeServer = mResult.getLong(ServerConnect.COLUMN_ROOMS_TIME);
                    timeLocal = RoomsDB.getRoomOpenTime(aud);
                    if (timeServer != timeLocal){ //время отличается, обновляем на устройстве
                        RoomsDB.updateRoom(new Room()
                                .setName(aud)
                                .setStatus(status)
                                .setUserRadioLabel(mResult.getString(ServerConnect.COLUMN_ROOMS_RADIO_LABEL))
                                .setUserName(mResult.getString(ServerConnect.COLUMN_ROOMS_LAST_VISITER))
                                .setOpenTime(mResult.getLong(ServerConnect.COLUMN_ROOMS_TIME))
                                .setAccess(mResult.getInt(ServerConnect.COLUMN_ROOMS_ACCESS)));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

       if (progressDialog !=null && progressDialog.isShowing()){
           progressDialog.setProgress(values[0]);
        }
   }

    private String getInClause(ArrayList items){
        StringBuilder inClause = new StringBuilder();
        if (items.size() != 0){
            for (int i=0; i < items.size(); i++) {
                if (items.get(i).getClass().equals(String.class)){
                    inClause.append("'").append(items.get(i)).append("'");
                } else {
                    inClause.append(items.get(i));
                }
                inClause.append(',');
            }
            if (inClause.length() == 0){
                inClause.append(0);
            } else {
                inClause.delete(inClause.length()-1,inClause.length());
            }
            return inClause.toString();
        } else {
            return "'0'";
        }

    }

    @Override
    protected void onPostExecute(Exception e) {
        super.onPostExecute(e);

        if (callback !=null){
            if (e == null){
                callback.onSuccessServerRead();
            }else{
                callback.onErrorServerRead(e);
            }
        }
        if (progressDialog !=null && progressDialog.isShowing()){
            progressDialog.cancel();
        }
    }

    public interface Callback{
        void onSuccessServerRead();
        void onErrorServerRead(Exception e);
    }

}
