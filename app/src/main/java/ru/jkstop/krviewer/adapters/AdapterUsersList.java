package ru.jkstop.krviewer.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import ru.jkstop.krviewer.R;
import ru.jkstop.krviewer.RecyclerItemClickListener;
import ru.jkstop.krviewer.items.User;

public class AdapterUsersList extends RecyclerView.Adapter<AdapterUsersList.ViewHolder>{

    public static final int SHOW_LOCAL_USERS = 0;
    public static final int SHOW_SERVER_USERS = 1;

    private ArrayList <User> mUsersList;
    private int mType;
    private Context mContext;
    private RecyclerItemClickListener mClickListener;

    public AdapterUsersList(Context c, ArrayList<User> usersList, int type, RecyclerItemClickListener listener) {
        mUsersList = usersList;
        mContext = c;
        mType = type;
        mClickListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView userImage;
        public TextView userInitials, userDivision;

        public ViewHolder(View itemView) {
            super(itemView);
            userImage = (ImageView)itemView.findViewById(R.id.user_card_photo);
            userInitials = (TextView)itemView.findViewById(R.id.user_card_initials);
            userDivision = (TextView)itemView.findViewById(R.id.user_card_division);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_user_item,parent,false);
        final ViewHolder viewHolder = new ViewHolder(rowView);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemClick(v, viewHolder.getLayoutPosition());
            }
        });
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.userInitials.setText(mUsersList.get(position).getInitials());
        holder.userDivision.setText(mUsersList.get(position).getDivision());

        Picasso.with(mContext)
                .load(new File(mUsersList.get(position).getPhotoPath()))
                .fit()
                .centerCrop()
                .placeholder(R.drawable.ic_user_not_found)
                .into(holder.userImage);
    }

    @Override
    public int getItemCount() {
        return mUsersList.size();
    }

}
