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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

import ru.jkstop.krviewer.R;
import ru.jkstop.krviewer.databases.JournalDB;
import ru.jkstop.krviewer.databases.UsersDB;
import ru.jkstop.krviewer.items.JournalItem;
import ru.jkstop.krviewer.items.Room;

/**
 * Фрагмент журнал
 */
public class JournalFragment extends Fragment {

    private RecyclerView recycler;
    private static ArrayList<JournalItem> journalItems;
    private AdapterJournalList adapterJournalList;

    private ProgressBar progressBar;

    private static Handler handler;

    private Context context;

    private static final int HIDE_PROGRESS = 10;
    private static final int UPDATE_LIST = 11;

    public static JournalFragment newInstance(){
        return new JournalFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        journalItems = new ArrayList<>();

        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case HIDE_PROGRESS:
                        progressBar.setVisibility(View.INVISIBLE);
                        break;
                    case UPDATE_LIST:
                        adapterJournalList.notifyDataSetChanged();
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
        recycler.setLayoutManager(new GridLayoutManager(context, getResources().getInteger(R.integer.columns_journal)));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.recycler_main, container, false);

        progressBar = (ProgressBar)fragmentView.findViewById(R.id.main_progress_bar);

        recycler = (RecyclerView) fragmentView.findViewById(R.id.main_recycler);
        adapterJournalList = new AdapterJournalList();
        recycler.setAdapter(adapterJournalList);
        recycler.setLayoutManager(new GridLayoutManager(context, getResources().getInteger(R.integer.columns_journal)));

        return fragmentView;
    }

    public static Thread loadJournalTask(final Date date){
        return new Thread(new Runnable() {
            @Override
            public void run() {

                if (!journalItems.isEmpty()) journalItems.clear();
                journalItems.addAll(JournalDB.getJournalItems(date));

                handler.sendEmptyMessage(HIDE_PROGRESS);
                handler.sendEmptyMessage(UPDATE_LIST);
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
            bindedItem = journalItems.get(position);

            holder.textUserName.setText(bindedItem.getUserName());
            holder.textRoomName.setText(bindedItem.getRoomName());
            holder.textOpenTime.setText(String.valueOf(new Time(bindedItem.getOpenTime())));

            if (bindedItem.getCloseTime() == 0){
                holder.textCloseTime.setText(getString(R.string.room_opened));
            } else {
                holder.textCloseTime.setText(String.valueOf(new Time(bindedItem.getCloseTime())));
            }

            if (bindedItem.getAccess() == Room.ACCESS_CLICK){
                holder.imageAccess.setImageResource(R.drawable.ic_no_card_18dp);
            } else {
                holder.imageAccess.setImageResource(R.drawable.ic_card_18dp);
            }

            Picasso.with(context)
                    .load(UsersDB.getUserPhoto(journalItems.get(position).getUserRadioLabel()))
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.img_user_not_found)
                    .into(holder.imageUser);
        }

        @Override
        public int getItemCount() {
            return journalItems.size();
        }

        @Override
        public long getItemId(int position) {
            return journalItems.get(position).getOpenTime();
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
