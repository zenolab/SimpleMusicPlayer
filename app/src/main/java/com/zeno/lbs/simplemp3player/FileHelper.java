package com.zeno.lbs.simplemp3player;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import static com.zeno.lbs.simplemp3player.MainActivity.LOG_TAG;
import static com.zeno.lbs.simplemp3player.MyApplication.songList;

import static com.zeno.lbs.simplemp3player.MyApplication.mySongsPlay;

/**
 * Created by grd on 1/29/18.
 */

public class FileHelper {

    private String[] items;

    private ArrayList<File> findSong(File root) {
        ArrayList<File> at = new ArrayList<File>();
        File[] files = root.listFiles();
        for (File singleFile : files) {
            if (singleFile.isDirectory() && !singleFile.isHidden()) {
                at.addAll(findSong(singleFile));
            } else {
                if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")) {
                    at.add(singleFile);
                }
            }
        }
        return at;
    }

    void checkExternalStorage(Context context) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            MyApplication.mExternalStorageAvailable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            MyApplication.mExternalStorageAvailable = true;
        } else {
            MyApplication.mExternalStorageAvailable = false;
        }
        handleExternalStorageState(context);

    }

    void handleExternalStorageState(Context context) {
        if (MyApplication.mExternalStorageAvailable) {
            loadTrackList();
        } else {
            Toast.makeText(context, "Please insert an SDcard", Toast.LENGTH_LONG).show();
        }
    }

    void loadTrackList() {
        final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());
        mySongsPlay = mySongs;
        items = new String[mySongs.size()];
        Song song;
        for (int i = 0; i < mySongs.size(); i++) {
            song = new Song(items[i] = mySongs.get(i).getName().toString());
            songList.add(song);
        }

    }

}
