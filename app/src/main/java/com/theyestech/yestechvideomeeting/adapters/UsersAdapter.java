package com.theyestech.yestechvideomeeting.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.theyestech.yestechvideomeeting.R;
import com.theyestech.yestechvideomeeting.models.Users;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder>{
    private List<Users> users;

    public UsersAdapter(List<Users> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UsersViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        holder.setUsersData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UsersViewHolder extends RecyclerView.ViewHolder {

        TextView tv_FirstChar, tv_Username, tv_Email;
        ImageView iv_VideoMeeting, iv_AudioMeeting;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_FirstChar = itemView.findViewById(R.id.tv_FirstChar);
            tv_Username = itemView.findViewById(R.id.tv_Username);
            tv_Email = itemView.findViewById(R.id.tv_Email);
            iv_VideoMeeting = itemView.findViewById(R.id.iv_VideoMeeting);
            iv_AudioMeeting = itemView.findViewById(R.id.iv_AudioMeeting);

        }

        void setUsersData(Users users){
            tv_FirstChar.setText(users.firstname.substring(0,1));
            tv_Username.setText(String.format("%s %s", users.firstname, users.lastname));
            tv_Email.setText(users.email);
        }
    }
}
