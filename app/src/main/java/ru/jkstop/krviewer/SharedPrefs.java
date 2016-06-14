package ru.jkstop.krviewer;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Настройки
 */
public class SharedPrefs {


    private static final String ACTIVE_ACCOUNT_ID = "active_account_id";
    private static final String ACTIVE_ACCOUNT_EMAIL = "active_account_email";
    private static final String SERVER_NAME = "server_name";

    private static SharedPreferences mPreferences;
    private static SharedPreferences.Editor mPreferencesEditor;

    private static String getStringFromRes(int strId){
        return App.getAppContext().getResources().getString(strId);
    }

    public SharedPrefs(){

        mPreferences = getPreferences();
        mPreferencesEditor = getPreferencesEditor();
    }

    private static SharedPreferences getPreferences(){
        if (mPreferences == null){
            mPreferences = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        }
        return mPreferences;
    }

    private static SharedPreferences.Editor getPreferencesEditor(){
        if (mPreferencesEditor == null){
            mPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(App.getAppContext()).edit();
        }
        return mPreferencesEditor;
    }

    public static String showDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy",new Locale("ru"));
        return String.valueOf(dateFormat.format(new Date())) + " г.";
    }

    public static void setActiveAccountID(String accountID){
        getPreferencesEditor().putString(ACTIVE_ACCOUNT_ID, accountID).apply();
    }

    public static String getActiveAccountID(){
        return getPreferences().getString(ACTIVE_ACCOUNT_ID, "");
    }

    public static void setActiveAccountEmail(String accountEmail){
        getPreferencesEditor().putString(ACTIVE_ACCOUNT_EMAIL, accountEmail).apply();
    }

    public static String getActiveAccountEmail(){
        return getPreferences().getString(ACTIVE_ACCOUNT_EMAIL, null);
    }

    public static void setServerName (String serverName){
        getPreferencesEditor().putString(SERVER_NAME, serverName).apply();
    }

    public static String getServerName(){
        return getPreferences().getString(SERVER_NAME, "-");
    }

}
