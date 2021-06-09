package com.example.screenrecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.modules.core.PermissionListener;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetActivityInterface;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.JitsiMeetViewListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class MeetingActivity extends AppCompatActivity implements JitsiMeetActivityInterface, JitsiMeetViewListener {
    private JitsiMeetView jitsiMeetView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);


        Intent retrieveIntent = getIntent();
        String room_id = retrieveIntent.getStringExtra("room_id");
        Log.e("room id", room_id);

        try {
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL(""))
                    .setWelcomePageEnabled(false)
                    .build();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                JitsiMeetConferenceOptions options
                        = new JitsiMeetConferenceOptions.Builder()
                        .setRoom(room_id)
                        .setFeatureFlag("recording.enabled", false)
                        .setFeatureFlag("live-streaming.enabled", false)
                        .setFeatureFlag("invite.enabled", false)
                        .setFeatureFlag("close-captions.enabled", false)
                        .setFeatureFlag("toolbox.enabled", false)
                        .setFeatureFlag("toolbox.enabled", false)
                        .build();

                JitsiMeetActivity.launch(MeetingActivity.this, options);


            }
        }, 5000);


    }

    @Override
    public void requestPermissions(String[] strings, int i, PermissionListener permissionListener) {

    }

    @Override
    public void onConferenceJoined(Map<String, Object> map) {

    }

    @Override
    public void onConferenceTerminated(Map<String, Object> map) {
            Log.e("FINISH","NNOW");
//
//        Intent intent = new Intent();
//
//        setResult(RESULT_OK, intent);
//
//        finish();
    }

    @Override
    public void onConferenceWillJoin(Map<String, Object> map) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}