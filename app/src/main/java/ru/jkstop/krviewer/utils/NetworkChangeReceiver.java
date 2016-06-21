package ru.jkstop.krviewer.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.jkstop.krviewer.MainActivity;
import ru.jkstop.krviewer.utils.NetworkUtil;

/**
 * Ресивер смена сети
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = NetworkUtil.getConnectivityStatusString(context);
        if (MainActivity.handler!=null){
            MainActivity.handler.sendEmptyMessage(status);
        }
    }

}
