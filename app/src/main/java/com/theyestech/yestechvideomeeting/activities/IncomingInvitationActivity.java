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

import com.google.firebase.iid.FirebaseInstanceId;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingInvitationActivity extends AppCompatActivity {
    private View view;
    private Context context;

    private ImageView iV_MeetingType, iv_AcceptInvitation, iv_RejectInvitation;
    private TextView tv_FirstChar, tv_Username, tv_Email;
    private String meetingType = null;
    private Users users;

    private PreferenceManager preferenceManager;
    private String inviterToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_invitation);
        context = this;
        //users = (Users) getIntent().getSerializableExtra("users");
        initializeUI();
    }

    private void initializeUI() {
        iV_MeetingType = findViewById(R.id.iV_MeetingType);
        iv_AcceptInvitation = findViewById(R.id.iv_AcceptInvitation);
        iv_RejectInvitation = findViewById(R.id.iv_RejectInvitation);
        tv_FirstChar = findViewById(R.id.tv_FirstChar);
        tv_Username = findViewById(R.id.tv_Username);
        tv_Email = findViewById(R.id.tv_Email);

        meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);
        preferenceManager = new PreferenceManager(context);
        if (meetingType != null) {
            if (meetingType.equals("video")) {
                iV_MeetingType.setImageResource(R.drawable.ic_video);
            }else{
                iV_MeetingType.setImageResource(R.drawable.ic_audio);
            }
        }

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                inviterToken = task.getResult().getToken();
            }
        });
        String firstname = getIntent().getStringExtra(Constants.KEY_FIRST_NAME);
        if (firstname != null) {
            tv_FirstChar.setText(firstname.substring(0, 1));
        }
        tv_Username.setText(String.format("%s %s", firstname, getIntent().getStringExtra(Constants.KEY_LAST_NAME)));
        tv_Email.setText(getIntent().getStringExtra(Constants.KEY_EMAIL));

        iv_AcceptInvitation.setOnClickListener(v -> sendInvitationResponse(Constants.REMOTE_MSG_INVITATION_ACCEPTED, getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)));
        iv_RejectInvitation.setOnClickListener(v -> sendInvitationResponse(Constants.REMOTE_MSG_INVITATION_REJECTED, getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)));
    }

    private void sendInvitationResponse(String type, String receiverToken) {

        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), type);

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }

    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(Constants.getRemoteMessageHeader(), remoteMessageBody)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                                try {
                                    URL serverURL = new URL("https://meet.jit.si");
                                    JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                                    builder.setServerURL(serverURL);
                                    builder.setWelcomePageEnabled(false);
                                    builder.setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM));
                                    if(meetingType.equals("audio")){
                                        builder.setVideoMuted(true);
                                    }
                                    JitsiMeetActivity.launch(context, builder.build());
                                    finish();
                                }catch (Exception e){
                                    Toast.makeText(context,"FUCK " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(context, "Invitation rejected", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }


    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if(type != null){
                if(type.equals(Constants.REMOTE_MSG_INVITATION_CANCELLED)){
                    Toast.makeText(context, "Invitation Cancelled", Toast.LENGTH_LONG).show();
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
