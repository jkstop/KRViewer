package ru.jkstop.krviewer;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ru.jkstop.krviewer.items.App;

/**
 * Настройки
 */
public class SharedPrefs {


    private static final String ACTIVE_ACCOUNT_ID = "active_account_id";
    private static final String SERVER_NAME = "server_name";

    private static SharedPreferences preferences;
    private static SharedPreferences.Editor preferenceEditor;


    public SharedPrefs(){

        preferences = getPreferences();
        preferenceEditor = getPreferencesEditor();
    }

    private static SharedPreferences getPreferences(){
        if (preferences == null){
            preferences = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        }
        return preferences;
    }

    private static SharedPreferences.Editor getPreferencesEditor(){
        if (preferenceEditor == null){
            preferenceEditor = PreferenceManager.getDefaultSharedPreferences(App.getAppContext()).edit();
        }
        return preferenceEditor;
    }

    public static void setActiveAccountID(String accountID){
        getPreferencesEditor().putString(ACTIVE_ACCOUNT_ID, accountID).apply();
    }

    public static String getActiveAccountID(){
        return getPreferences().getString(ACTIVE_ACCOUNT_ID, App.getAppContext().getString(R.string.log_on));
    }

    public static void setServerName (String serverName){
        getPreferencesEditor().putString(SERVER_NAME, serverName).apply();
    }

    public static String getServerName(){
        return getPreferences().getString(SERVER_NAME, "10.38.2.6");
    }

}
