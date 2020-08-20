package com.theyestech.yestechvideomeeting.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.theyestech.yestechvideomeeting.R;
import com.theyestech.yestechvideomeeting.models.Users;
import com.theyestech.yestechvideomeeting.network.ApiClient;
import com.theyestech.yestechvideomeeting.network.ApiService;
import com.theyestech.yestechvideomeeting.utils.Constants;
import com.theyestech.yestechvideomeeting.utils.PreferenceManager;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingInvitationActivity extends AppCompatActivity {
    private View view;
    private Context context;

    private ImageView iV_MeetingType, iv_StopInvitation;
    private TextView tv_FirstChar, tv_Username, tv_Email;
    private String meetingType = null;
    private Users users;

    private PreferenceManager preferenceManager;
    private String inviterToken = null, meetingRoom = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);
        context = this;
        initializeUI();
    }

    private void initializeUI() {
        iV_MeetingType = findViewById(R.id.iV_MeetingType);
        tv_FirstChar = findViewById(R.id.tv_FirstChar);
        tv_Username = findViewById(R.id.tv_Username);
        tv_Email = findViewById(R.id.tv_Email);
        iv_StopInvitation = findViewById(R.id.iv_StopInvitation);

        users = (Users) getIntent().getSerializableExtra("users");
        meetingType = getIntent().getStringExtra("type");
        preferenceManager = new PreferenceManager(context);


        if (meetingType != null) {
            if (meetingType.equals("video")) {
                iV_MeetingType.setImageResource(R.drawable.ic_video);
            }else{
                iV_MeetingType.setImageResource(R.drawable.ic_audio);
            }
        }
        if (users != null) {
            tv_FirstChar.setText(users.firstname.substring(0, 1));
            tv_Username.setText(String.format("%s %s", users.firstname, users.lastname));
            tv_Email.setText(users.email);
        }
        iv_StopInvitation.setOnClickListener(v -> {
            if(users != null){
                cancelInvitation(users.token);
            }
        });
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null){
                inviterToken = task.getResult().getToken();
                if(meetingType != null && users != null){
                    initiateMeeting(meetingType, users.token);
                }

            }
        });

    }

    private void initiateMeeting(String meetingType, String receiverToken){
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_FIRST_NAME, preferenceManager.getString(Constants.KEY_FIRST_NAME));
            data.put(Constants.KEY_LAST_NAME, preferenceManager.getString(Constants.KEY_LAST_NAME));
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);

            meetingRoom = preferenceManager.getString(Constants.KEY_USER_ID) + "_" + UUID.randomUUID().toString().substring(0,5);
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(Constants.getRemoteMessageHeader(), remoteMessageBody)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call,@NonNull  Response<String> response) {
                        if(response.isSuccessful()){
                            if(type.equals(Constants.REMOTE_MSG_INVITATION)){
                                Toast.makeText(context, "Invitation sent successfully", Toast.LENGTH_LONG).show();
                            }else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){
                                Toast.makeText(context, "Invitation cancelled", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }else  {
                            Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull  Call<String> call,@NonNull  Throwable t) {
                        Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    private void cancelInvitation(String receiverToken) {

        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE);

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }

    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if(type != null){
                if(type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                    try {
                        URL serverURL = new URL("https://meet.jit.si");
                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverURL);
                        builder.setWelcomePageEnabled(false);
                        builder.setRoom(meetingRoom);
                        if(meetingType.equals("audio")){
                            builder.setVideoMuted(true);
                        }
                        JitsiMeetActivity.launch(context, builder.build());
                        finish();
                    }catch (Exception e){
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)){
                    Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(context).registerReceiver(invitationResponseReceiver, new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(invitationResponseReceiver);
    }
}
