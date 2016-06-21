package ru.jkstop.krviewer;

import android.os.StrictMode;
import android.support.annotation.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Соединение с сервером
 */
public class ServerConnect {

    //таблицы
    public static final String JOURNAL_TABLE = "JOURNAL_V2";
    public static final String PERSONS_TABLE = "PERSONS";
    public static final String ROOMS_TABLE = "ROOMS";
    public static final String ALL_STAFF_TABLE = "STAFF_NEW";

    //колонки
    public static final String COLUMN_JOURNAL_ACCOUNT_ID = "ACCOUNT_ID";
    public static final String COLUMN_JOURNAL_ROOM = "ROOM";
    public static final String COLUMN_JOURNAL_TIME_IN = "TIME_IN";
    public static final String COLUMN_JOURNAL_TIME_OUT = "TIME_OUT";
    public static final String COLUMN_JOURNAL_ACCESS = "ACCESS";
    public static final String COLUMN_JOURNAL_PERSON_INITIALS = "PERSON_INITIALS";
    public static final String COLUMN_JOURNAL_PERSON_TAG = "PERSON_TAG";

    public static final String COLUMN_PERSONS_ACCOUNT_ID = "ACCOUNT_ID";
    public static final String COLUMN_PERSONS_LASTNAME = "LASTNAME";
    public static final String COLUMN_PERSONS_FIRSTNAME = "FIRSTNAME";
    public static final String COLUMN_PERSONS_MIDNAME = "MIDNAME";
    public static final String COLUMN_PERSONS_DIVISION = "DIVISION";
    public static final String COLUMN_PERSONS_TAG = "TAG";
    public static final String COLUMN_PERSONS_SEX = "SEX";
    public static final String COLUMN_PERSONS_ACCESS = "ACCESS";
    public static final String COLUMN_PERSONS_PHOTO_BASE64 = "PHOTO_BASE64";

    public static final String COLUMN_ROOM_ACCOUNT_ID = "ACCOUNT_ID";
    public static final String COLUMN_ROOMS_ROOM = "ROOM";
    public static final String COLUMN_ROOMS_STATUS = "STATUS";
    public static final String COLUMN_ROOMS_ACCESS = "ACCESS";
    public static final String COLUMN_ROOMS_TIME = "TIME";
    public static final String COLUMN_ROOMS_LAST_VISITER = "LAST_VISITER";
    public static final String COLUMN_ROOMS_RADIO_LABEL = "RADIO_LABEL";
    public static final String COLUMN_ROOMS_PHOTO = "PHOTO";

    public static final String COLUMN_ALL_STAFF_DIVISION = "NAME_DIVISION";
    public static final String COLUMN_ALL_STAFF_LASTNAME = "LASTNAME";
    public static final String COLUMN_ALL_STAFF_FIRSTNAME = "FIRSTNAME";
    public static final String COLUMN_ALL_STAFF_MIDNAME = "MIDNAME";
    public static final String COLUMN_ALL_STAFF_SEX = "SEX";
    public static final String COLUMN_ALL_STAFF_PHOTO = "PHOTO";
    public static final String COLUMN_ALL_STAFF_TAG = "RADIO_LABEL";

    private static Connection sqlconnect;
    private static Thread connectThread;
    private static int callingTask;

    private static String serverName;
    private static Callback callback;

    private static final String NET_SOURCEFORGE_JTDS_JDBC_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
    private static final String DB = "KeyRegistratorBase";

    public ServerConnect(String serverName, Callback callback){
        ServerConnect.serverName = serverName;
        ServerConnect.callback = callback;
    }


    public static void getConnection(String serverName, int callingTask, @Nullable Callback callback){
        ServerConnect.serverName = serverName;
        ServerConnect.callback = callback;
        ServerConnect.callingTask = callingTask;

        System.out.println("SQL CONNECT " + sqlconnect);
        System.out.println("THREAD " + connectThread);

        try {
            if (serverName == null){
                if (sqlconnect !=null && !sqlconnect.isClosed()){
                    callback.onServerConnected(sqlconnect, ServerConnect.callingTask);
                } else {
                    if (connectThread == null){
                        connectThread = new Thread(null, getConnect, "MSSQLServerConnect");
                        ServerConnect.serverName = SharedPrefs.getServerName();

                        connectThread.start();
                    }
                }
            } else {
                if (connectThread == null){
                    connectThread = new Thread(null, getConnect, "MSSQLServerConnect");
                    connectThread.start();
                }
            }
        } catch (Exception e){
            callback.onServerConnectException(e);
        }
    }

    public static void closeConnection(){
        if (sqlconnect !=null){
            new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sqlconnect.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
            }).start();
        }
    }

    private static Runnable getConnect = new Runnable() {
        @Override
        public void run() {
            getConnectionFromUrl(serverName, callback);
        }
    };


    private static void getConnectionFromUrl(final String serverName, final Callback callback){

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Class.forName(NET_SOURCEFORGE_JTDS_JDBC_DRIVER);
            final String ConnURL = "jdbc:jtds:sqlserver://" + serverName + ";"
                    + "database=" + DB +";user=shsupport;password=podderzhka;loginTimeout=3";
            connectThread = null;
            sqlconnect = DriverManager.getConnection(ConnURL);
            if (callback!=null) callback.onServerConnected(sqlconnect, callingTask);
        } catch (Exception e) {
            e.printStackTrace();
            if (callback!=null) callback.onServerConnectException(e);
        }
    }

    public interface Callback{
        void onServerConnected(Connection connection, int callingTask);
        void onServerConnectException(Exception e);
    }
}
