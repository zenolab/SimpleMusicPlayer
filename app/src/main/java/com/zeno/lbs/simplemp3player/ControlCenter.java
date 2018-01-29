package com.zeno.lbs.simplemp3player;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import static com.zeno.lbs.simplemp3player.MainActivity.LOG_TAG;


import static com.zeno.lbs.simplemp3player.MyApplication.mp;
import static com.zeno.lbs.simplemp3player.MyApplication.mySongsPlay;
import static com.zeno.lbs.simplemp3player.MyApplication.positionPlay;
import static com.zeno.lbs.simplemp3player.MyApplication.sb;

/**
 * Created by grd on 1/29/18.
 */

public class ControlCenter {

    Context context;

    public ControlCenter(Context context) {
        this.context = context;
    }



    // local variable pause is accessed from within inner class; needs to be declared final
   // void controlPanel(final Button pause,Button forward,Button previous,Button next,Button reverse) {
    void controlPanel( Button pause,Button forward,Button previous,Button next,Button reverse) {
        //---------------------------------------------------------------------------
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());
                Log.d(LOG_TAG,"-- onStopTrackingTouch --");
            }

        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sb.setMax(mp.getDuration());

                if(mp.isPlaying()){
                    //// local variable pause is accessed from within inner class; needs to be declared final
                    MyApplication. pause.setText(">");
                    mp.pause();
                }
                else {
                    MyApplication.pause.setText("||");
                    mp.start();
                }

            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sb.setMax(mp.getDuration());

                mp.seekTo(mp.getCurrentPosition()+5000);
                Log.d(LOG_TAG,"-- pos  +5000 --");
            }
        });
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sb.setMax(mp.getDuration());

                mp.seekTo(mp.getCurrentPosition()-5000);
                Log.d(LOG_TAG,"--pos  -5000 --");
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.stop();
                mp.release();
                positionPlay=((positionPlay+1)%mySongsPlay.size());
                Uri u = Uri.parse(mySongsPlay.get( positionPlay).toString());
                mp = MediaPlayer.create(context,u);
                mp.start();
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.stop();
                mp.release();
                positionPlay=((positionPlay-1)<0)?(mySongsPlay.size()-1):(positionPlay-1);
                Uri u = Uri.parse(mySongsPlay.get( positionPlay).toString());//%mysongs so that it do not go to invalid position
                mp = MediaPlayer.create(context,u);

                //my
                //sb.setProgress(0);//set to zero,invalidate
                mp.start();
            }
        });
        //---------------------------------------------------------------------------
    }
}
