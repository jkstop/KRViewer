package ru.jkstop.krviewer.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;

import ru.jkstop.krviewer.R;
import ru.jkstop.krviewer.adapters.AdapterUsersList;
import ru.jkstop.krviewer.databases.UsersDB;
import ru.jkstop.krviewer.dialogs.DialogUserPhoto;
import ru.jkstop.krviewer.interfaces.RecyclerItemClickListener;
import ru.jkstop.krviewer.items.User;

/**
 * Фрагмент пользователи
 */
public class UsersFragment extends Fragment implements RecyclerItemClickListener {

    private static final int HIDE_PROGRESS = 10;
    private static final int UPDATE = 11;

    private RecyclerView recycler;
    private Context context;

    private static ArrayList<User> userList;
    private AdapterUsersList adapterUsersList;

    private ProgressBar progressBar;

    private static Handler handler;

    public static UsersFragment newInstance(){
        return new UsersFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getContext();

        userList = new ArrayList<>();

        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case HIDE_PROGRESS:
                        if (progressBar !=null) progressBar.setVisibility(View.INVISIBLE);
                        break;
                    case UPDATE:
                        if (adapterUsersList !=null) adapterUsersList.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recycler.setLayoutManager(new GridLayoutManager(context, getResources().getInteger(R.integer.columns_users)));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.recycler_main, container, false);

        progressBar = (ProgressBar)fragmentView.findViewById(R.id.main_progress_bar);
        recycler = (RecyclerView) fragmentView.findViewById(R.id.main_recycler);
        adapterUsersList = new AdapterUsersList(context, userList, this);

        recycler.setAdapter(adapterUsersList);
        recycler.setLayoutManager(new GridLayoutManager(context, getResources().getInteger(R.integer.columns_users)));

        loadUsersTask().start();

        return fragmentView;
    }



    public static Thread loadUsersTask (){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                if (!userList.isEmpty()) userList.clear();
                userList.addAll(UsersDB.getUserList());

                handler.sendEmptyMessage(HIDE_PROGRESS);
                handler.sendEmptyMessage(UPDATE);
            }
        });
    }

    @Override
    public void onItemClick(int position) {

        DialogUserPhoto.newInstance(userList.get(position).getInitials(), userList.get(position).getRadioLabel())
                .show(getChildFragmentManager(), getString(R.string.title_dialog_user_photo));
    }
}
