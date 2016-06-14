package ru.jkstop.krviewer;

import android.app.Application;
import android.content.Context;

/**
 * Created by ivsmirnov on 14.06.2016.
 */
public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getAppContext(){
        return context;
    }
}
