package com.zeno.lbs.simplemp3player;

import android.app.Application;
import android.media.MediaPlayer;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by grd on 1/29/18.
 */

public class MyApplication extends Application {

    static Button pause,forward,reverse,next,previous;

    static MediaPlayer mp;//assigning memory loc once or else multiple songs will play at once
    static SeekBar sb;

    static int positionPlay;


    public static Boolean mExternalStorageAvailable,permission=false;
    public static List<Song> songList = new ArrayList<>();
    public static ArrayList<File> mySongsPlay;
}
