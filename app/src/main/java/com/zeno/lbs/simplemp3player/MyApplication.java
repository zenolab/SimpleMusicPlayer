package com.zeno.lbs.simplemp3player;

import android.app.Application;
import android.media.MediaPlayer;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    //Temp memory leak var.
    static Button pause, forward, reverse, next, previous;
    static MediaPlayer mp;
    static SeekBar sb;
    static int positionPlay;
    static Boolean mExternalStorageAvailable, permission = false;
    static List<Song> songList = new ArrayList<>();
    static ArrayList<File> mySongsPlay;
}
