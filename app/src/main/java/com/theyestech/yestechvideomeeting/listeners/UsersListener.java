package com.theyestech.yestechvideomeeting.listeners;

import com.theyestech.yestechvideomeeting.models.Users;

public interface UsersListener {

    void initiateVideoMeeting(Users users);

    void initiateAudioMeeting(Users users);

    void onMultipleUsersAction(Boolean isMultipleUsersSelected);
}
