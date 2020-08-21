package com.theyestech.yestechvideomeeting.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.theyestech.yestechvideomeeting.R;
import com.theyestech.yestechvideomeeting.listeners.UsersListener;
import com.theyestech.yestechvideomeeting.models.Users;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {
    private List<Users> users;
    private UsersListener usersListener;
    private List<Users> selectedUsers;

    public UsersAdapter(List<Users> users, UsersListener usersListener) {
        this.users = users;
        this.usersListener = usersListener;
        selectedUsers = new ArrayList<>();
    }

    public List<Users> getSelectedUsers() {
        return selectedUsers;
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

    class UsersViewHolder extends RecyclerView.ViewHolder {

        TextView tv_FirstChar, tv_Username, tv_Email;
        ImageView iv_VideoMeeting, iv_AudioMeeting, iv_ImageSelected;
        ConstraintLayout userContainer;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_FirstChar = itemView.findViewById(R.id.tv_FirstChar);
            tv_Username = itemView.findViewById(R.id.tv_Username);
            tv_Email = itemView.findViewById(R.id.tv_Email);
            iv_VideoMeeting = itemView.findViewById(R.id.iv_VideoMeeting);
            iv_AudioMeeting = itemView.findViewById(R.id.iv_AudioMeeting);
            userContainer = itemView.findViewById(R.id.userContainer);
            iv_ImageSelected = itemView.findViewById(R.id.iv_ImageSelected);
        }

        void setUsersData(Users users) {
            tv_FirstChar.setText(users.firstname.substring(0, 1));
            tv_Username.setText(String.format("%s %s", users.firstname, users.lastname));
            tv_Email.setText(users.email);
            iv_AudioMeeting.setOnClickListener(v -> usersListener.initiateAudioMeeting(users));
            iv_VideoMeeting.setOnClickListener(v -> usersListener.initiateVideoMeeting(users));

            userContainer.setOnLongClickListener(v -> {
                if (iv_ImageSelected.getVisibility() != View.VISIBLE) {
                    selectedUsers.add(users);
                    iv_ImageSelected.setVisibility(View.VISIBLE);
                    iv_VideoMeeting.setVisibility(View.GONE);
                    iv_AudioMeeting.setVisibility(View.GONE);
                    usersListener.onMultipleUsersAction(true);
                }
                return true;
            });
            userContainer.setOnClickListener(v -> {
                if (iv_ImageSelected.getVisibility() == View.VISIBLE) {
                    selectedUsers.remove(users);
                    iv_ImageSelected.setVisibility(View.GONE);
                    iv_VideoMeeting.setVisibility(View.VISIBLE);
                    iv_AudioMeeting.setVisibility(View.VISIBLE);
                    if (selectedUsers.size() == 0) {
                        usersListener.onMultipleUsersAction(false);
                    }
                } else {
                    if (selectedUsers.size() > 0) {
                        selectedUsers.add(users);
                        iv_ImageSelected.setVisibility(View.VISIBLE);
                        iv_VideoMeeting.setVisibility(View.GONE);
                        iv_AudioMeeting.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}
