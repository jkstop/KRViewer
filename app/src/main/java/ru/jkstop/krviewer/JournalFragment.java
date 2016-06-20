package ru.jkstop.krviewer;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

import ru.jkstop.krviewer.databases.JournalDB;
import ru.jkstop.krviewer.databases.UsersDB;
import ru.jkstop.krviewer.items.JournalItem;
import ru.jkstop.krviewer.items.Room;

/**
 * Фрагмент журнал
 */
public class JournalFragment extends Fragment {

    private static ArrayList<JournalItem> mJournalList;
    private RecyclerView mRecycler;
    private AdapterJournalList mAdapter;

    private ProgressBar mProgressBar;

    private static Handler mHandler;

    private Context context;

    private static final int HIDE_PROGRESS = 10;
    private static final int UPDATE_LIST = 11;

    public static JournalFragment newInstance(){
        System.out.println("new journal fragment");
        return new JournalFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        mJournalList = new ArrayList<>();

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
        mAdapter = new AdapterJournalList();
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        return fragmentView;
    }

    public static Thread loadJournalTask(final Date date){
        return new Thread(new Runnable() {
            @Override
            public void run() {

                if (!mJournalList.isEmpty()) mJournalList.clear();
                mJournalList.addAll(JournalDB.getJournalItems(date));

                mHandler.sendEmptyMessage(HIDE_PROGRESS);
                mHandler.sendEmptyMessage(UPDATE_LIST);
            }
        });
    }

    private class AdapterJournalList extends RecyclerView.Adapter<AdapterJournalList.ViewHolderJournalItem> {

        private JournalItem bindedItem;

        public AdapterJournalList() {}

        @Override
        public ViewHolderJournalItem onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_journal_item,parent,false);
            return new ViewHolderJournalItem(rowView);
        }

        @Override
        public void onBindViewHolder(ViewHolderJournalItem holder, int position) {
            bindedItem = mJournalList.get(position);

            holder.textUserName.setText(bindedItem.getUserName());
            holder.textRoomName.setText(bindedItem.getRoomName());
            holder.textOpenTime.setText(String.valueOf(new Time(bindedItem.getOpenTime())));

            if (bindedItem.getCloseTime() == 0){
                holder.textCloseTime.setText("Открыто");
            } else {
                holder.textCloseTime.setText(String.valueOf(new Time(bindedItem.getCloseTime())));
            }

            if (bindedItem.getAccess() == Room.ACCESS_CLICK){
                holder.imageAccess.setImageResource(R.drawable.ic_touch_app_white_18dp);
            } else {
                holder.imageAccess.setImageResource(R.drawable.ic_credit_card_white_18dp);
            }

            Picasso.with(context)
                    .load(UsersDB.getUserPhoto(mJournalList.get(position).getUserRadioLabel()))
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_not_found)
                    .into(holder.imageUser);
        }

        @Override
        public int getItemCount() {
            return mJournalList.size();
        }

        @Override
        public long getItemId(int position) {
            return mJournalList.get(position).getOpenTime();
        }

        class ViewHolderJournalItem extends RecyclerView.ViewHolder{

            public TextView textRoomName, textOpenTime, textCloseTime, textUserName;
            public ImageView imageUser, imageAccess;

            public ViewHolderJournalItem(View itemView) {
                super(itemView);
                textRoomName = (TextView)itemView.findViewById(R.id.journal_card_room);
                textOpenTime = (TextView)itemView.findViewById(R.id.journal_card_time_in);
                textCloseTime = (TextView)itemView.findViewById(R.id.journal_card_time_out);
                textUserName = (TextView)itemView.findViewById(R.id.journal_card_user);
                imageUser = (ImageView)itemView.findViewById(R.id.journal_card_photo);
                imageAccess = (ImageView)itemView.findViewById(R.id.journal_card_access_icon);

            }
        }
    }

}
