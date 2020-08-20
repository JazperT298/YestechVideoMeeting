package com.theyestech.yestechvideomeeting.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.theyestech.yestechvideomeeting.R;
import com.theyestech.yestechvideomeeting.models.Users;

public class OutgoingInvitationActivity extends AppCompatActivity {
    private View view;
    private Context context;

    private ImageView iV_MeetingType,iv_StopInvitation;
    private TextView tv_FirstChar, tv_Username, tv_Email;
    private String meetingType;
    private Users users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);
        context = this;
        users = (Users) getIntent().getSerializableExtra("users");
        meetingType = getIntent().getStringExtra("type");
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeUI();
    }

    private void initializeUI(){
        iV_MeetingType = findViewById(R.id.iV_MeetingType);
        tv_FirstChar = findViewById(R.id.tv_FirstChar);
        tv_Username = findViewById(R.id.tv_Username);
        tv_Email = findViewById(R.id.tv_Email);
        iv_StopInvitation = findViewById(R.id.iv_StopInvitation);
        if(meetingType != null){
            if(meetingType.equals("video")){
                iV_MeetingType.setImageResource(R.drawable.ic_video);
            }
        }
        if(users != null){
            tv_FirstChar.setText(users.firstname.substring(0,1));
            tv_Username.setText(String.format("%s %s", users.firstname, users.lastname));
            tv_Email.setText(users.email);
        }
        iv_StopInvitation.setOnClickListener(v -> onBackPressed());
    }
}
