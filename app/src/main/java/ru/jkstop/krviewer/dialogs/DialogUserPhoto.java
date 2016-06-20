package ru.jkstop.krviewer.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import ru.jkstop.krviewer.R;
import ru.jkstop.krviewer.SearchFragment;
import ru.jkstop.krviewer.UsersFragment;
import ru.jkstop.krviewer.databases.UsersDB;
import ru.jkstop.krviewer.items.ImageSaver;

/**
 * Полноэкранное фото пользователя
 */
public class DialogUserPhoto extends DialogFragment {

    private static final String BUNDLE_USER_RADIO_LABEL = "bundle_user_radio_label";
    private static final String BUNDLE_USER_NAME = "bundle_user_name";

    private String userRadioLabel, userName;

    public static DialogUserPhoto newInstance(String userName, String userRadioLabel){
        DialogUserPhoto dialogUserPhoto = new DialogUserPhoto();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_USER_RADIO_LABEL, userRadioLabel);
        bundle.putString(BUNDLE_USER_NAME, userName);
        dialogUserPhoto.setArguments(bundle);
        return dialogUserPhoto;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,android.R.style.Theme_DeviceDefault_NoActionBar);

        userRadioLabel = getArguments().getString(BUNDLE_USER_RADIO_LABEL);
        userName = getArguments().getString(BUNDLE_USER_NAME);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.view_user_photo, container, false);

        Toolbar toolbar = (Toolbar)dialogView.findViewById(R.id.user_photo_toolbar);
        toolbar.setTitle(userName);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });

        ImageView imageContainer = (ImageView)dialogView.findViewById(R.id.user_photo_image_container);

        File userPhoto;
        if (getParentFragment() instanceof UsersFragment){
            userPhoto = UsersDB.getUserPhoto(userRadioLabel);
        } else if (getParentFragment() instanceof SearchFragment){
            userPhoto = new File(getContext().getFilesDir() + ImageSaver.DIRECTORY_TEMP, userRadioLabel + ImageSaver.PREFIX_WEBP);
        } else {
            userPhoto = new File("");
        }

        Picasso.with(getContext())
                .load(userPhoto)
                .into(imageContainer);
        return dialogView;
    }
}
