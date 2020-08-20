package com.theyestech.yestechvideomeeting.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.theyestech.yestechvideomeeting.R;
import com.theyestech.yestechvideomeeting.models.Users;
import com.theyestech.yestechvideomeeting.utils.Constants;
import com.theyestech.yestechvideomeeting.utils.PreferenceManager;

public class IncomingInvitationActivity extends AppCompatActivity {
    private View view;
    private Context context;

    private ImageView iV_MeetingType, iv_AcceptInvitation, iv_RejectInvitation;
    private TextView tv_FirstChar, tv_Username, tv_Email;
    private String meetingType;
    private Users users;

    private PreferenceManager preferenceManager;
    private String inviterToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_invitation);
        context = this;
        //users = (Users) getIntent().getSerializableExtra("users");
        meetingType = getIntent().getStringExtra("type");
        preferenceManager = new PreferenceManager(context);

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null){
                inviterToken = task.getResult().getToken();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeUI();
    }

    private void initializeUI(){
        iV_MeetingType = findViewById(R.id.iV_MeetingType);
        iv_AcceptInvitation = findViewById(R.id.iv_AcceptInvitation);
        iv_RejectInvitation = findViewById(R.id.iv_RejectInvitation);
        tv_FirstChar = findViewById(R.id.tv_FirstChar);
        tv_Username = findViewById(R.id.tv_Username);
        tv_Email = findViewById(R.id.tv_Email);
        if (meetingType != null) {
            if (meetingType.equals("video")) {
                iV_MeetingType.setImageResource(R.drawable.ic_video);
            }
        }
        String firstname = getIntent().getStringExtra(Constants.KEY_FIRST_NAME);
        if(firstname != null){
            tv_FirstChar.setText(firstname.substring(0,1));
        }
        tv_Username.setText(String.format("%s %s", firstname, getIntent().getStringExtra(Constants.KEY_LAST_NAME)));
        tv_Email.setText(getIntent().getStringExtra(Constants.KEY_EMAIL));

    }
}
