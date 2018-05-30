package com.zeno.lbs.simplemp3player;

//com.zeno.lbs.simplemp3player
// http://www.androidhive.info/2016/01/android-working-with-recycler-view/
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zeno.lbs.simplemp3player.adapter.ClickListener;
import com.zeno.lbs.simplemp3player.adapter.DividerItemDecoration;
import com.zeno.lbs.simplemp3player.adapter.RecyclerTouchListener;
import com.zeno.lbs.simplemp3player.adapter.SongsAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.zeno.lbs.simplemp3player.MyApplication;


import static com.zeno.lbs.simplemp3player.MyApplication.pause;
import static com.zeno.lbs.simplemp3player.MyApplication.forward;
import static com.zeno.lbs.simplemp3player.MyApplication.reverse;
import static com.zeno.lbs.simplemp3player.MyApplication.next;
import static com.zeno.lbs.simplemp3player.MyApplication.previous;

import static com.zeno.lbs.simplemp3player.MyApplication.mp;
import static com.zeno.lbs.simplemp3player.MyApplication.sb;
import static com.zeno.lbs.simplemp3player.MyApplication.positionPlay;
import static com.zeno.lbs.simplemp3player.MyApplication.mySongsPlay;
import static com.zeno.lbs.simplemp3player.MyApplication.songList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongsAdapter mAdapter;

    Context context;

    //------------------------------
    public static final String LOG_TAG = "Tunes_log";

   // Boolean mExternalStorageAvailable,permission=false;
   // String[] items;//to read all files
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 99;

    //===============================================

    Thread updateSeekBar;
   // Button pause,forward,reverse,next,previous;

    TextView textView;
    TextView textName;
    TextView textTime;
    //-----------------------------------------------
    //Handles headphones coming unplugged.
    // cannot be done through a manifest receiver
    //https://stackoverflow.com/questions/29032029/media-player-should-stop-on-disconnecting-headphone-in-my-android-app-programati?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( mp != null && mp.isPlaying() ) {
                mp.pause();
            }
        }
    };


    //------------------------------

    @Override
    protected void onCreate(@ Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //=================**** mp3 ****===================
        //===================================
        textTime = (TextView) findViewById(R.id.songTime);
        textName = (TextView) findViewById(R.id.text);


        pause = (Button)findViewById(R.id.pause);
        forward = (Button)findViewById(R.id.forward);
        previous = (Button)findViewById(R.id.previous);
        next = (Button)findViewById(R.id.next);
        reverse = (Button)findViewById(R.id.reverse);
        sb=(SeekBar)findViewById(R.id.seekBar);
        //===================================

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new SongsAdapter(songList);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator()); // animations
        recyclerView.setAdapter(mAdapter);

        //custom item listener for RecylerView
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                selectedSong(position);

            }
            @Override
            public void onLongClick(View view, int position) {
                Toast.makeText(getApplicationContext(),  " LongClick !!!", Toast.LENGTH_SHORT).show();
            }
        } ));


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MyApplication.permission = checkStoragePermission();
        }
        else
            new FileHelper().loadTrackList();
        if(MyApplication.permission){
            new FileHelper().loadTrackList();
        }

        //---------------------FOR disconnecting headphone
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);


    } //--- End onCreate()

    //------------------- life cycle---------------
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "MainActivity: onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "MainActivity: onResume()");
         new ControlCenter(context).controlPanel(pause,forward,previous,next,reverse);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "MainActivity: onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "MainActivity: onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "MainActivity: onDestroy()");
        unregisterReceiver(mNoisyReceiver);
    }

    //----------------Permission-----------------------
    public boolean checkStoragePermission() {

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
            return false;
        } else {
            return true;
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        new FileHelper().checkExternalStorage(MainActivity.this);
                    }
                } else {
                    Toast.makeText(this, "permission denied",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    void selectedSong(int position){

        Song song = songList.get(position);
        Toast.makeText(getApplicationContext(), song.getTitle() + " is selected!", Toast.LENGTH_SHORT).show();
        try {

            updateSeekBar = new Thread() {


                @Override
                public void run() {
                    Log.d(LOG_TAG,"--####### updateSeekBar--########");
                    int runtime = mp.getDuration();
                    int currentPosition = 0;
                    // advance - продвижение
                    int advance = 0; // progress
                         while ((advance = ((advance = runtime - currentPosition) < 100) ? advance : 100) > 2) {
                        try {
                            currentPosition = mp.getCurrentPosition();
                            if (sb != null) {
                                sb.setProgress(currentPosition);
                            }
                            sleep(advance);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (IllegalStateException e) {
                            sb.setProgress(runtime);
                            break;
                        }

                        Log.d(LOG_TAG,"--Track Position  --is ==  "+currentPosition);
                        Log.e(LOG_TAG,"--Track Position  --is ==  "+runtime);
                        int remain = runtime-currentPosition;
                        Log.w(LOG_TAG,"--Track Position  --is ==  "+remain);

                         //-----avto load next
                        if(remain <= 175){
                            mp.release();
                            positionPlay=((positionPlay+1)%mySongsPlay.size()); // список треков
                            Uri u = Uri.parse(mySongsPlay.get( positionPlay).toString()); //перейти к конкретному треку
                            mp = MediaPlayer.create(context,u);
                            mp.start();

                        }

                    }
                }
            };

        } catch (Exception exception) {
            Log.d(LOG_TAG,"--Fall out Exception -- "+exception.getMessage());
        }

        //----------------------------------------------------------------------------------
        if(mp != null){
            mp.stop();
            mp.release();
            Log.d(LOG_TAG,"--STOP & release()--");

        }

        positionPlay = position;
        Uri u = Uri.parse(mySongsPlay.get(position).toString()); // java.lang.IndexOutOfBoundsException: Index: 15, Size: 12  -- Out Of Bounds  - За границами

        Log.d(LOG_TAG,"--Uri.parse -- " + u);
        textName.setText("One "+ mySongsPlay.get(position).toString());


        mp = MediaPlayer.create(getApplicationContext(),u);
        Log.d(LOG_TAG,"--MediaPlayer.CREATE--");

        mp.start();

        textTime.setText(""+mp.getDuration());

        sb.setMax(mp.getDuration());
        updateSeekBar.start();

        //----------------------------------------------------------------------------------
    }

}