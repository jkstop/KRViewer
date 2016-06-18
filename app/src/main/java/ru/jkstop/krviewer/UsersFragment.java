package ru.jkstop.krviewer;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
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
 * Фрагмент пользователи
 */
public class UsersFragment extends Fragment implements RecyclerItemClickListener{

    private static final int HIDE_PROGRESS = 10;
    public static final int UPDATE = 11;

    private Context context;

    private static ArrayList<User> mUsersList;
    private RecyclerView mRecycler;
    private AdapterUsersList mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ProgressBar mProgressBar;

    public static Handler handler;

    public static UsersFragment newInstance(){
        System.out.println("new users fragment");
        return new UsersFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getContext();

        mUsersList = new ArrayList<>();

        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case HIDE_PROGRESS:
                        if (mProgressBar!=null) mProgressBar.setVisibility(View.INVISIBLE);
                        break;
                    case UPDATE:
                        if (mAdapter!=null) mAdapter.notifyDataSetChanged();
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



    public static Thread loadUsersTask (){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                if (!mUsersList.isEmpty()) mUsersList.clear();
                mUsersList.addAll(UsersDB.getUserList());

                handler.sendEmptyMessage(HIDE_PROGRESS);
                handler.sendEmptyMessage(UPDATE);
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        System.out.println("item click " + position);
    }
}
