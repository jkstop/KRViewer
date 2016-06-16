package ru.jkstop.krviewer;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import java.util.ArrayList;

import ru.jkstop.krviewer.databases.RoomsDB;
import ru.jkstop.krviewer.items.Room;

/**
 * Фрагмент загруженность помещений
 */
public class LoadRoomFragment extends Fragment implements RecyclerItemClickListener {

    private ArrayList<Room> mRoomList;
    private RecyclerView mRecycler;
    private AdapterRoomsList mAdapter;

    private ProgressBar mProgressBar;

    private Handler mHandler;

    private Context context;

    public static LoadRoomFragment newInstance(){
        System.out.println("new rooms fragment");
        return new LoadRoomFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        mRoomList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.recycler_main, container, false);
        mRecycler = (RecyclerView)fragmentView.findViewById(R.id.main_recycler);

        mRoomList.addAll(RoomsDB.getRoomList());

        for (int i=0;i<100;i++){
            mRoomList.add(new Room().setName("Room " + i).setUserName("Lastname Firstname Midname").setStatus(Room.STATUS_FREE));
        }

        mProgressBar = (ProgressBar)fragmentView.findViewById(R.id.main_progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);

        mAdapter = new AdapterRoomsList();
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        return fragmentView;
    }

    @Override
    public void onItemClick(View view, int position) {

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
            return mRoomList.get(position).getStatus();
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
                    ((auditroomFreeViewHolder) holder).freeRoomText.setText(mRoomList.get(position).getName());
                    break;
                case 0:
                    ((auditroomBusyViewHolder)holder).busyRoomName.setText(mRoomList.get(position).getName());
                    ((auditroomBusyViewHolder)holder).busyRoomUser.setText(mRoomList.get(position).getUserName());

                    Picasso.with(context)
                            .load(new File(mRoomList.get(position).getUserPhotoPath()))
                            .fit()
                            .centerCrop()
                            .placeholder(R.drawable.ic_user_not_found)
                            .into(((auditroomBusyViewHolder)holder).busyRoomImage);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return mRoomList.size();
        }


    }

}
