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

import java.util.ArrayList;

import ru.jkstop.krviewer.R;
import ru.jkstop.krviewer.databases.RoomsDB;
import ru.jkstop.krviewer.databases.UsersDB;
import ru.jkstop.krviewer.items.Room;

/**
 * Фрагмент загруженность помещений
 */
public class LoadRoomFragment extends Fragment {

    private static ArrayList<Room> roomList;
    private RecyclerView recycler;
    private AdapterRoomsList adapterRoomsList;

    private ProgressBar progressBar;

    private static final int HIDE_PROGRESS = 10;
    private static final int UPDATE = 11;
    private static Handler handler;

    private Context context;

    public static LoadRoomFragment newInstance(){
        return new LoadRoomFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        roomList = new ArrayList<>();

        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case HIDE_PROGRESS:
                        if (progressBar !=null) progressBar.setVisibility(View.INVISIBLE);
                        break;
                    case UPDATE:
                        if (adapterRoomsList !=null) adapterRoomsList.notifyDataSetChanged();
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
        recycler.setLayoutManager(new GridLayoutManager(context, getResources().getInteger(R.integer.columns_load_rooms)));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.recycler_main, container, false);
        recycler = (RecyclerView) fragmentView.findViewById(R.id.main_recycler);

        progressBar = (ProgressBar)fragmentView.findViewById(R.id.main_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        adapterRoomsList = new AdapterRoomsList();
        recycler.setAdapter(adapterRoomsList);
        recycler.setLayoutManager(new GridLayoutManager(context, getResources().getInteger(R.integer.columns_load_rooms)));

        loadRoomsTask().start();

        return fragmentView;
    }

    public static Thread loadRoomsTask(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                if (!roomList.isEmpty()) roomList.clear();
                roomList.addAll(RoomsDB.getRoomList());

                handler.sendEmptyMessage(HIDE_PROGRESS);
                handler.sendEmptyMessage(UPDATE);
            }
        });
    }

    private class AdapterRoomsList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public AdapterRoomsList() {}

        class auditroomFreeViewHolder extends RecyclerView.ViewHolder{
            public ImageView freeRoomImage;
            public TextView freeRoomText;

            public auditroomFreeViewHolder(View itemView) {
                super(itemView);
                freeRoomImage = (ImageView)itemView.findViewById(R.id.room_free_card_image);
                freeRoomText = (TextView)itemView.findViewById(R.id.room_free_card_text);
            }
        }

        class auditroomBusyViewHolder extends RecyclerView.ViewHolder{
            public ImageView busyRoomImage;
            public TextView busyRoomName, busyRoomUser;

            public auditroomBusyViewHolder(View itemView) {
                super(itemView);
                busyRoomImage = (ImageView)itemView.findViewById(R.id.room_busy_user_image);
                busyRoomName = (TextView)itemView.findViewById(R.id.room_busy_text_room);
                busyRoomUser = (TextView)itemView.findViewById(R.id.room_busy_text_user);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return roomList.get(position).getStatus();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {

            View itemView;
            RecyclerView.ViewHolder viewHolder = null;
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

            switch (viewType){
                case 1:
                    itemView = layoutInflater.inflate(R.layout.view_room_free,parent,false);
                    viewHolder = new auditroomFreeViewHolder(itemView);
                    break;
                case 0:
                    itemView = layoutInflater.inflate(R.layout.view_room_busy,parent,false);
                    viewHolder = new auditroomBusyViewHolder(itemView);
                    break;
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            switch (holder.getItemViewType()){
                case 1:
                    ((auditroomFreeViewHolder) holder).freeRoomText.setText(roomList.get(position).getName());
                    break;
                case 0:
                    ((auditroomBusyViewHolder)holder).busyRoomName.setText(roomList.get(position).getName());
                    ((auditroomBusyViewHolder)holder).busyRoomUser.setText(roomList.get(position).getUserName());

                    Picasso.with(context)
                            .load(UsersDB.getUserPhoto(roomList.get(position).getUserRadioLabel()))
                            .fit()
                            .centerCrop()
                            .placeholder(R.drawable.img_user_not_found)
                            .into(((auditroomBusyViewHolder)holder).busyRoomImage);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return roomList.size();
        }


    }

}
