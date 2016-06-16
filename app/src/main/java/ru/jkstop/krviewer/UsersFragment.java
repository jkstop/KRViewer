package ru.jkstop.krviewer;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;

import ru.jkstop.krviewer.adapters.AdapterUsersList;
import ru.jkstop.krviewer.databases.UsersDB;
import ru.jkstop.krviewer.items.User;

/**
 * Фрагмент пользователи
 */
public class UsersFragment extends Fragment implements RecyclerItemClickListener{

    private static final int HIDE_PROGRESS = 10;
    private static final int UPDATE_LIST = 11;

    private Context context;

    private ArrayList<User> mUsersList;
    private RecyclerView mRecycler;
    private AdapterUsersList mAdapter;

    private ProgressBar mProgressBar;

    private Handler mHandler;

    public static UsersFragment newInstance(){
        System.out.println("new users fragment");
        return new UsersFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getContext();

        mUsersList = new ArrayList<>();

        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case HIDE_PROGRESS:
                        mProgressBar.setVisibility(View.INVISIBLE);
                        break;
                    case UPDATE_LIST:
                        mAdapter.notifyDataSetChanged();
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
        View fragmentView = inflater.inflate(R.layout.recycler_main, container, false);

        mProgressBar = (ProgressBar)fragmentView.findViewById(R.id.main_progress_bar);
        mRecycler = (RecyclerView)fragmentView.findViewById(R.id.main_recycler);
        mAdapter = new AdapterUsersList(context, mUsersList, 0, this);

        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        loadUsersTask().start();

        return fragmentView;
    }

    private void loadUsersList(){
        if (!mUsersList.isEmpty()) mUsersList.clear();
        mUsersList.addAll(UsersDB.getUserList());
        for (int i=0;i<100;i++){
            mUsersList.add(new User().setInitials("Lastname Firstname Midname " + i).setDivision("Division #" + i));
        }
    }

    private Thread loadUsersTask (){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                loadUsersList();
                mHandler.sendEmptyMessage(HIDE_PROGRESS);
                mHandler.sendEmptyMessage(UPDATE_LIST);
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        System.out.println("item click " + position);
    }
}
