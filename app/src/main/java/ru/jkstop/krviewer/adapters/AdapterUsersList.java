package ru.jkstop.krviewer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import ru.jkstop.krviewer.R;
import ru.jkstop.krviewer.interfaces.RecyclerItemClickListener;
import ru.jkstop.krviewer.items.User;

public class AdapterUsersList extends RecyclerView.Adapter<AdapterUsersList.ViewHolder>{

    private ArrayList <User> userList;

    private Context context;
    private RecyclerItemClickListener itemClickListener;

    public AdapterUsersList(Context c, ArrayList<User> usersList, RecyclerItemClickListener listener) {
        userList = usersList;
        context = c;
        itemClickListener = listener;
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
                itemClickListener.onItemClick(viewHolder.getLayoutPosition());
            }
        });
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.userInitials.setText(userList.get(position).getInitials());
        holder.userDivision.setText(userList.get(position).getDivision());

        Picasso.with(context)
                .load(new File(userList.get(position).getPhotoPath()))
                .fit()
                .centerCrop()
                .placeholder(R.drawable.img_user_not_found)
                .into(holder.userImage);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

}
