package ru.jkstop.krviewer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import ru.jkstop.krviewer.R;

/**
 * Диалог выход из аккаунта
 */
public class DialogLogOut extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDialog().cancel();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((Callback)getActivity()).onSigningOut();
                        getDialog().cancel();
                    }
                })
                .setTitle(getString(R.string.title_dialog_log_out))
                .setMessage(getString(R.string.body_dialog_log_out))
                .create();
    }

    public interface Callback{
        void onSigningOut();
    }
}
