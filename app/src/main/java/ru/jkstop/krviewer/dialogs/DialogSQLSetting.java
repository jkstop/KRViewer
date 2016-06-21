package ru.jkstop.krviewer.dialogs;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;

import java.sql.Connection;

import ru.jkstop.krviewer.items.App;
import ru.jkstop.krviewer.MainActivity;
import ru.jkstop.krviewer.R;
import ru.jkstop.krviewer.ServerConnect;
import ru.jkstop.krviewer.SharedPrefs;

/**
 * Настройка подключения MSSQL
 */
public class DialogSQLSetting extends DialogFragment implements ServerConnect.Callback {

    private static final int HANDLER_CONNECTED = 100;
    private static final int HANDLER_DISCONNECTED = 200;

    private Context context;
    private ServerConnect.Callback callback;
    private TextInputLayout textInputLayout;
    private EditText serverName;
    private ImageView serverStatus, serverCheckConnect;
    private Handler handler;
    private String connectError;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getContext();
        callback = this;

        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case HANDLER_CONNECTED:
                        serverCheckConnect.clearAnimation();
                        textInputLayout.setErrorEnabled(false);
                        serverStatus.setImageResource(R.drawable.ic_cloud_done_36dp);

                        SharedPrefs.setServerName(serverName.getText().toString());
                        break;
                    case HANDLER_DISCONNECTED:
                        serverCheckConnect.clearAnimation();
                        serverStatus.setImageResource(R.drawable.ic_cloud_off_36dp);
                        textInputLayout.setErrorEnabled(true);
                        textInputLayout.setError(connectError);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogLayout = View.inflate(context, R.layout.view_server_settings, null);

        getDialog().setTitle(getString(R.string.title_dialog_sql_connect));

        //анимация поворота стрелки
        final Animation rotationAnim = AnimationUtils.loadAnimation(context, R.anim.rotate);
        rotationAnim.setRepeatCount(Animation.INFINITE);

        textInputLayout = (TextInputLayout)dialogLayout.findViewById(R.id.dialog_sql_setting_input_server);
        serverName = textInputLayout.getEditText();
        serverCheckConnect = (ImageView)dialogLayout.findViewById(R.id.dialog_sql_setting_reconnect);
        serverStatus = (ImageView)dialogLayout.findViewById(R.id.dialog_sql_setting_status);

        serverName.setText(SharedPrefs.getServerName());

        serverCheckConnect.startAnimation(rotationAnim);

        connect(serverName.getText().toString(), callback);

        serverCheckConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textInputLayout.getEditText().getText().length() == 0){
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(App.getAppContext().getString(R.string.input_empty_error));
                } else {
                    serverCheckConnect.startAnimation(rotationAnim);
                    connect(serverName.getText().toString(), callback);
                }
            }
        });

        //если android по 4.4 включительно, то включаем программное ускорение
        //иначе анимация не работает
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
            serverCheckConnect.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        return dialogLayout;
    }

    private void connect (String serverName, ServerConnect.Callback callback){
        ServerConnect.getConnection(serverName, 0, callback);
    }

    @Override
    public void onServerConnected(Connection connection, int callingTask) {
        handler.sendEmptyMessage(HANDLER_CONNECTED);
        MainActivity.handler.sendEmptyMessage(MainActivity.SERVER_CONNECTED);
    }

    @Override
    public void onServerConnectException(Exception e) {
        connectError = e.getLocalizedMessage();
        handler.sendEmptyMessage(HANDLER_DISCONNECTED);
        MainActivity.handler.sendEmptyMessage(MainActivity.SERVER_DISCONNECTED);

    }
}
