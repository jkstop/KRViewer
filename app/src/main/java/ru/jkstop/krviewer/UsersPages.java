package ru.jkstop.krviewer;

import android.content.Context;
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

import ru.jkstop.krviewer.adapters.AdapterUsersList;
import ru.jkstop.krviewer.databases.UsersDB;
import ru.jkstop.krviewer.items.User;

/**
 * Created by ivsmirnov on 15.06.2016.
 */
public class UsersPages extends Fragment implements RecyclerItemClickListener{

    private static final String PAGE_NUMBER = "page_number";

    public static final int PAGE_LOCAL_USERS = 0;
    public static final int PAGE_SERVER_USERS = 1;

    private static final int HIDE_PROGRESS = 10;
    private static final int UPDATE_LIST = 11;

    private Context context;
    private int page;

    private ArrayList<User> mUsersList;
    private RecyclerView mRecycler;
    private AdapterUsersList mAdapter;

    private ProgressBar mProgressBar;

    private Handler mHandler;

    public static UsersPages newInstance(int page){
        UsersPages usersPages = new UsersPages();
        Bundle bundle = new Bundle();
        bundle.putInt(PAGE_NUMBER, page);
        usersPages.setArguments(bundle);
        return usersPages;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        page = getArguments().getInt(PAGE_NUMBER);
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
        View fragmentView;
        switch (page){
            case PAGE_LOCAL_USERS:
                fragmentView = inflater.inflate(R.layout.recycler_main, container, false);
                mProgressBar = (ProgressBar)fragmentView.findViewById(R.id.main_progress_bar);
                mRecycler = (RecyclerView)fragmentView.findViewById(R.id.main_recycler);
                mAdapter = new AdapterUsersList(context, mUsersList, 0, this);

                mRecycler.setAdapter(mAdapter);
                mRecycler.setLayoutManager(new GridLayoutManager(context, 1));

                loadUsersTask().start();
                return fragmentView;
            case PAGE_SERVER_USERS:
                fragmentView = inflater.inflate(R.layout.fragment_users, container, false);
                return fragmentView;
            default:
                return super.onCreateView(inflater, container, savedInstanceState);
        }
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
