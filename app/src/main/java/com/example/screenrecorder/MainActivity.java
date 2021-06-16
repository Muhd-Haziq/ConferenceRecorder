package com.example.screenrecorder;

import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.react.modules.core.PermissionListener;
import com.hbisoft.hbrecorder.HBRecorder;
import com.hbisoft.hbrecorder.HBRecorderListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.jitsi.meet.sdk.JitsiMeetActivityInterface;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.JitsiMeetViewListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivateKey;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity implements HBRecorderListener, JitsiMeetActivityInterface, JitsiMeetViewListener {


    private static final int SCREEN_RECORD_REQUEST_CODE = 100;
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 101;
    private static final int PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = 102;
    HBRecorder hbRecorder;
    Button btnStart,btnStop;
    EditText etRoomID;
    boolean hasPermissions;
    ContentValues contentValues;
    ContentResolver resolver;
    Uri mUri;


    // Initialize view to be used for the meeting
    private JitsiMeetView jitsiMeetView;

    // Initialize conference rooms
    private ListView roomListView;
    private ArrayList<ConferenceRoom> roomsArrayList;
    private ArrayList<String> roomNamesList;
    private ArrayAdapter<String> aaRooms;
    private ConferenceRoom selectedConferenceRoom;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hbRecorder = new HBRecorder(this, this);
        btnStart=findViewById(R.id.btnStart);
        btnStop=findViewById(R.id.btnStop);
        etRoomID = findViewById(R.id.etRoomID);
        roomListView = findViewById(R.id.listViewRooms);

        jitsiMeetView = new JitsiMeetView(MainActivity.this);


        /* ---------- INITIALIZING ROOM MANAGERS - START ---------- */

        roomsArrayList = new ArrayList<>();
        roomNamesList = new ArrayList<>();
        aaRooms = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roomNamesList);
        roomListView.setAdapter(aaRooms);

        refreshRooms();

        /* ---------- INITIALIZING ROOM MANAGERS - END -------------------- */

        roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedConferenceRoom = roomsArrayList.get(position);

                if(selectedConferenceRoom.getParticipants() < 2){
                    addRoomParticipants();
                }else{
                    Toast.makeText(MainActivity.this, "ROOM IS CURRENTLY FULL", Toast.LENGTH_SHORT).show();
                }

            }
        });

        /* ---------- INITIALIZING ROOM MANAGERS - END -------------------- */



        /* ---------- SETTING UP INITIAL JITSI CONFERENCE OPTIONS - START ---------- */

        try {
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL(""))
                    .setWelcomePageEnabled(false)
                    .build();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        /* ---------- SETTING UP INITIAL JITSI CONFERENCE OPTIONS - END -------------------- */


        hbRecorder.setVideoEncoder("H264");

        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String roomName = etRoomID.getText().toString();

                if(roomName.length() > 0) {
                    addConferenceRoom(roomName);
                }

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    //first check if permissions was granted
//                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE)) {
//                        hasPermissions = true;
//                    }
//                    if (hasPermissions) {
//                        String roomName = etRoomID.getText().toString();
//
//                        if(roomName.length() > 0){
////                            addConferenceRoom(roomName);
//
//
//
//                            startRecordingScreen();
//                            JitsiMeetConferenceOptions options
//                                    = new JitsiMeetConferenceOptions.Builder()
//                                    .setRoom(text)
//                                    .setFeatureFlag("live-streaming.enabled", false)
//                                    .setFeatureFlag("invite.enabled", false)
//                                    .setFeatureFlag("close-captions.enabled", false)
//                                    .setFeatureFlag("toolbox.enabled", false)
//                                    .build();
//                            jitsiMeetView.join(options);
//                            setContentView(jitsiMeetView);
//                            jitsiMeetView.setListener(MainActivity.this);
//
//
//                            timeLeftInMilliseconds = 10000;
//                            startTimer();
//
//                            final Handler handler = new Handler(Looper.getMainLooper());
//                            handler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//
//                                    JitsiMeetConferenceOptions options
//                                            = new JitsiMeetConferenceOptions.Builder()
//                                            .setRoom(text)
//                                            .setFeatureFlag("live-streaming.enabled", false)
//                                            .setFeatureFlag("invite.enabled", false)
//                                            .setFeatureFlag("close-captions.enabled", false)
//                                            .setFeatureFlag("toolbox.enabled", false)
//                                            .build();
//                                    jitsiMeetView.join(options);
//                                    setContentView(jitsiMeetView);
//                                    jitsiMeetView.setListener(MainActivity.this);
//                                    JitsiMeetActivity.launch(MainActivity.this,options);
//
//                                    Intent meeting = new Intent(MainActivity.this, MeetingActivity.class);
//                                    meeting.putExtra("room_id", text);
//                                    startActivity(meeting);
//                                }
//                            }, timeLeftInMilliseconds);
//
//                        }
//
//
//                    }
//                } else {
//                    //showLongToast("This library requires API 21>");
//                }
            }
        });


        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hbRecorder.stopScreenRecording();
            }
        });
    }

    @Override
    public void HBRecorderOnStart() {
        Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void HBRecorderOnComplete() {
        Toast.makeText(this, "Completed", Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Update gallery depending on SDK Level
            if (hbRecorder.wasUriSet()) {
                updateGalleryUri();
            }else{
                refreshGalleryFile();
            }
        }
    }

    @Override
    public void HBRecorderOnError(int errorCode, String reason) {
        Toast.makeText(this, errorCode+": "+reason, Toast.LENGTH_SHORT).show();
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startRecordingScreen() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent permissionIntent = mediaProjectionManager != null ? mediaProjectionManager.createScreenCaptureIntent() : null;
        startActivityForResult(permissionIntent, SCREEN_RECORD_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //Start screen recording
                hbRecorder.startScreenRecording(data, resultCode, this);
            }
        }
    }
    //For Android 10> we will pass a Uri to HBRecorder
    //This is not necessary - You can still use getExternalStoragePublicDirectory
    //But then you will have to add android:requestLegacyExternalStorage="true" in your Manifest
    //IT IS IMPORTANT TO SET THE FILE NAME THE SAME AS THE NAME YOU USE FOR TITLE AND DISPLAY_NAME

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setOutputPath() {
        String filename = generateFileName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver = getContentResolver();
            contentValues = new ContentValues();
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "SpeedTest/" + "SpeedTest");
            contentValues.put(MediaStore.Video.Media.TITLE, filename);
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            mUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            //FILE NAME SHOULD BE THE SAME
            hbRecorder.setFileName(filename);
            hbRecorder.setOutputUri(mUri);
        }else{
            createFolder();
            hbRecorder.setOutputPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) +"/HBRecorder");
        }
    }
    //Check if permissions was granted
    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }
    private void updateGalleryUri(){
        contentValues.clear();
        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0);
        getContentResolver().update(mUri, contentValues, null, null);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void refreshGalleryFile() {
        MediaScannerConnection.scanFile(this,
                new String[]{hbRecorder.getFilePath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }
    //Generate a timestamp to be used as a file name
    private String generateFileName() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate).replace(" ", "");
    }
    //drawable to byte[]
    private byte[] drawable2ByteArray(@DrawableRes int drawableId) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), drawableId);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
    //Create Folder
    //Only call this on Android 9 and lower (getExternalStoragePublicDirectory is deprecated)
    //This can still be used on Android 10> but you will have to add android:requestLegacyExternalStorage="true" in your Manifest
    private void createFolder() {
        File f1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "SpeedTest");
        if (!f1.exists()) {
            if (f1.mkdirs()) {
                Log.i("Folder ", "created");
            }
        }
    }

    @Override
    public void requestPermissions(String[] strings, int i, PermissionListener permissionListener) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    @Override
    public void onConferenceJoined(Map<String, Object> map) {
        if(hasPermissions){
            startRecordingScreen();
        }
    }

    @Override
    public void onConferenceTerminated(Map<String, Object> map) {
//        Log.e("TEST", "CONF END");
        setContentView(R.layout.activity_main);

        hbRecorder.stopScreenRecording();
        removeRoomParticipants();
        startActivity(Intent.makeRestartActivityTask(MainActivity.this.getIntent().getComponent()));
    }

    @Override
    public void onConferenceWillJoin(Map<String, Object> map) {

    }



    /* ------------------------- ROOM MANAGER - START ------------------------- */

    // -> Creation of rooms upon booking
    private void addConferenceRoom(String roomName){
        boolean isDuplicate = false;

        for(String name : roomNamesList){
            if(roomName.equalsIgnoreCase(name)){
                isDuplicate = true;
            }
        }

        if(!isDuplicate){
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("room_name", roomName);

            // http://10.0.2.2/fyp_telepharmacy/addConferenceRoom.php

            client.post("https://fyp-conference.azurewebsites.net/addConferenceRoom.php", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Toast.makeText(MainActivity.this, "ADDED", Toast.LENGTH_SHORT).show();
                    refreshRooms();
                }
            });
        }else{
            Toast.makeText(MainActivity.this, "ROOM ALREADY EXISTS", Toast.LENGTH_SHORT).show();
        }

    }

    // -> Incrementing amount of participants by 1
    private void addRoomParticipants(){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("id", selectedConferenceRoom.getId());

        // http://10.0.2.2/fyp_telepharmacy/updateConferenceParticipantsUp.php

        client.get("https://fyp-conference.azurewebsites.net/updateConferenceParticipantsUp.php", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(MainActivity.this, "JOINED", Toast.LENGTH_SHORT).show();
                refreshRooms();


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // first check if permissions was granted
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE)) {
                        hasPermissions = true;
                    }
                    if (hasPermissions) {
                        JitsiMeetConferenceOptions options
                                = new JitsiMeetConferenceOptions.Builder()
                                .setRoom(selectedConferenceRoom.getRoom_name())
                                .setFeatureFlag("live-streaming.enabled", false)
                                .setFeatureFlag("invite.enabled", false)
                                .setFeatureFlag("close-captions.enabled", false)
                                .setFeatureFlag("toolbox.enabled", false)
                                .build();
                        jitsiMeetView.join(options);
                        setContentView(jitsiMeetView);
                        jitsiMeetView.setListener(MainActivity.this);
                    }
                } else {
                    //showLongToast("This library requires API 21>");
                }
            }
        });
    }


    // -> Decrementing amount of participants by 1
    private void removeRoomParticipants(){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("id", selectedConferenceRoom.getId());

        // http://10.0.2.2/fyp_telepharmacy/updateConferenceParticipantsDown.php
        client.get("https://fyp-conference.azurewebsites.net/updateConferenceParticipantsDown.php", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(MainActivity.this, "LEFT", Toast.LENGTH_SHORT).show();
                refreshRooms();
            }
        });
    }

    // -> Refresh list of available rooms
    private void refreshRooms(){
        AsyncHttpClient client = new AsyncHttpClient();
        // http://10.0.2.2/fyp_telepharmacy/getRooms.php

        client.get("https://fyp-conference.azurewebsites.net/getRooms.php", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                roomsArrayList.clear();
                roomNamesList.clear();

                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject conferenceRoom = (JSONObject) response.get(i);
                        ConferenceRoom room = new ConferenceRoom(conferenceRoom.getInt("id"), conferenceRoom.getString("room_name"),  conferenceRoom.getInt("participants"));
                        roomsArrayList.add(room);
                        roomNamesList.add(room.getRoom_name());
                    }
                } catch (JSONException e) {

                }
                aaRooms.notifyDataSetChanged();

//                super.onSuccess(statusCode, headers, response);
            }
        });
    }

    /* ------------------------- ROOM MANAGER - END -------------------------------------------------- */


}