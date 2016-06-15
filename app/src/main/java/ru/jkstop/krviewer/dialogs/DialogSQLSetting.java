package ru.jkstop.krviewer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;

import java.sql.Connection;

import ru.jkstop.krviewer.App;
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

    private Context mContext;
    private ServerConnect.Callback mCallback;
    private TextInputLayout mInputServerName;
    private EditText mServerName;
    private ImageView mServerStatus,mCheckServerConnect;
    private Handler mHandler;
    private String connectError;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();
        mCallback = this;

        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case HANDLER_CONNECTED:
                        mCheckServerConnect.clearAnimation();
                        mInputServerName.setErrorEnabled(false);
                        mServerStatus.setImageResource(R.drawable.ic_cloud_done_black_36dp);

                        SharedPrefs.setServerName(mServerName.getText().toString());
                        break;
                    case HANDLER_DISCONNECTED:
                        mCheckServerConnect.clearAnimation();
                        mServerStatus.setImageResource(R.drawable.ic_cloud_off_black_36dp);
                        mInputServerName.setErrorEnabled(true);
                        mInputServerName.setError(connectError);
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
        View dialogLayout = View.inflate(mContext, R.layout.dialog_sql_setting, null);

        getDialog().setTitle(getString(R.string.title_dialog_sql_connect));

        //анимация поворота стрелки
        final Animation rotationAnim = AnimationUtils.loadAnimation(mContext, R.anim.rotate);
        rotationAnim.setRepeatCount(Animation.INFINITE);

        mInputServerName = (TextInputLayout)dialogLayout.findViewById(R.id.dialog_sql_setting_input_server);
        mServerName = mInputServerName.getEditText();
        mCheckServerConnect = (ImageView)dialogLayout.findViewById(R.id.dialog_sql_setting_reconnect);
        mServerStatus = (ImageView)dialogLayout.findViewById(R.id.dialog_sql_setting_status);

        mServerName.setText(SharedPrefs.getServerName());

        mCheckServerConnect.startAnimation(rotationAnim);

        connect(mServerName.getText().toString(), mCallback);

        mCheckServerConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInputServerName.getEditText().getText().length() == 0){
                    mInputServerName.setErrorEnabled(true);
                    mInputServerName.setError(App.getAppContext().getString(R.string.input_empty_error));
                } else {
                    mCheckServerConnect.startAnimation(rotationAnim);
                    connect(mServerName.getText().toString(), mCallback);
                }
            }
        });

        //если android по 4.4 включительно, то включаем программное ускорение
        //иначе анимация не работает
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
            mCheckServerConnect.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        return dialogLayout;
    }

    private void connect (String serverName, ServerConnect.Callback callback){
        ServerConnect.getConnection(serverName, 0, callback);
    }

    @Override
    public void onServerConnected(Connection connection, int callingTask) {
        mHandler.sendEmptyMessage(HANDLER_CONNECTED);
        MainActivity.handler.sendEmptyMessage(MainActivity.SERVER_CONNECTED);
    }

    @Override
    public void onServerConnectException(Exception e) {
        connectError = e.getLocalizedMessage();
        mHandler.sendEmptyMessage(HANDLER_DISCONNECTED);
        MainActivity.handler.sendEmptyMessage(MainActivity.SERVER_DISCONNECTED);

    }
}
