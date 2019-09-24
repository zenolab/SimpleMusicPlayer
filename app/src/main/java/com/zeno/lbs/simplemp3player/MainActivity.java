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

    private Context context;

    private static final String LOG_TAG = "Tunes_log";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 99;
    private Thread updateSeekBar;
    private TextView textView;
    private TextView textName;
    private TextView textTime;
    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mp != null && mp.isPlaying()) {
                mp.pause();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textTime = (TextView) findViewById(R.id.songTime);
        textName = (TextView) findViewById(R.id.text);

        pause = (Button) findViewById(R.id.pause);
        forward = (Button) findViewById(R.id.forward);
        previous = (Button) findViewById(R.id.previous);
        next = (Button) findViewById(R.id.next);
        reverse = (Button) findViewById(R.id.reverse);
        sb = (SeekBar) findViewById(R.id.seekBar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new SongsAdapter(songList);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator()); // animations
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(),
                recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                selectedSong(position);

            }

            @Override
            public void onLongClick(View view, int position) {
                Toast.makeText(getApplicationContext(), " LongClick !!!", Toast.LENGTH_SHORT).show();
            }
        }));


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MyApplication.permission = checkStoragePermission();
        } else
            new FileHelper().loadTrackList();
        if (MyApplication.permission) {
            new FileHelper().loadTrackList();
        }
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new ControlCenter(context).controlPanel(pause, forward, previous, next, reverse);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNoisyReceiver);
    }

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

    void selectedSong(int position) {
        Song song = songList.get(position);
        Toast.makeText(getApplicationContext(), song.getTitle() + " is selected!", Toast.LENGTH_SHORT).show();
        try {
            updateSeekBar = new Thread() {


                @Override
                public void run() {
                    int runtime = mp.getDuration();
                    int currentPosition = 0;
                    int advance = 0;
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
                        int remain = runtime - currentPosition;
                        if (remain <= 175) {
                            mp.release();
                            positionPlay = ((positionPlay + 1) % mySongsPlay.size());
                            Uri u = Uri.parse(mySongsPlay.get(positionPlay).toString());
                            mp = MediaPlayer.create(context, u);
                            mp.start();
                        }
                    }
                }
            };
        } catch (Exception exception) {
            throw new RuntimeException("Cannot selected track"+exception.getMessage());
        }

        if (mp != null) {
            mp.stop();
            mp.release();
        }
        positionPlay = position;
        Uri u = Uri.parse(mySongsPlay.get(position).toString());
        textName.setText("One " + mySongsPlay.get(position).toString());
        mp = MediaPlayer.create(getApplicationContext(), u);
        mp.start();
        textTime.setText("" + mp.getDuration());
        sb.setMax(mp.getDuration());
        updateSeekBar.start();
    }

}