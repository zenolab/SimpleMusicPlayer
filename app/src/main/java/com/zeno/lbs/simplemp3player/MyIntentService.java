package com.zeno.lbs.simplemp3player;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import static com.zeno.lbs.simplemp3player.MyApplication.mp;
import static com.zeno.lbs.simplemp3player.MyApplication.mySongsPlay;
import static com.zeno.lbs.simplemp3player.MyApplication.positionPlay;
import static com.zeno.lbs.simplemp3player.MyApplication.sb;
import static com.zeno.lbs.simplemp3player.MyApplication.songList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class MyIntentService extends IntentService {

    private final String TAG ="MyIntetService";


    Context context;


    public MyIntentService() {
        super("MyIntentService");
    }

    public MyIntentService(Context context) {
        super("MyIntentService");

        this.context = context;
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        int tm = intent.getIntExtra("time", 0);
        //selectedSong(tm);
        /*
        int tm = intent.getIntExtra("time", 0);
        String label = intent.getStringExtra("task");
        Log.d(TAG, "onHandleIntent start: " + label);
        try {
            TimeUnit.SECONDS.sleep(tm);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onHandleIntent end: " + label);
        */
    }


    /*
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }
    */

    //========================================================================
    void selectedSong(int position){

        Song song = songList.get(position);
        Toast.makeText(getApplicationContext(), song.getTitle() + " is selected!", Toast.LENGTH_SHORT).show();

        // все равно может вылелеть если клатцать очень-очень быстро преключение треков,но относитльно вполне годится -- но пака что не вылетает!
        try {

            //!! НЕПРАВИЛЬНО СЧИТАЕТСЯ progressBar
            new MainActivity().updateSeekBar = new Thread() {


                @Override
                public void run() {
                    Log.d(TAG,"--####### updateSeekBar--########");
                    int runtime = mp.getDuration();
                    int currentPosition = 0;
                    // advance - продвижение
                    int advance = 0; // progress
                    // делать действие если есть более 2 миллисекунд - трека

                    // логическоеУсловие ? выражение1 : выражение2
                    // Если логическоеУсловие равно true, то вычисляется выражение1 и его результат становится результатом выполнения всего оператора.
                    // Если же логическоеУсловие равно false, то вычисляется выражение2, и его значение становится результатом работы оператора.
                    // while ((advance = ((advance = runtime - currentPosition) < 500) ? advance : 500) > 2) {
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

                        Log.d(TAG,"--Track Position  --is ==  "+currentPosition);
                        Log.e(TAG,"--Track Position  --is ==  "+runtime);
                        int remain = runtime-currentPosition;
                        Log.w(TAG,"--Track Position  --is ==  "+remain);

                        // из-за того что время текущего может быть минусовое трэк может заранее перейти на следующий
                        // понять причину почему текущеe время может быть с отрецательным интервалом
                        //if(remain <= 0){
                        if(remain == 0){
                            //new ControlCenter
                            //mp.stop(); // E/AndroidRuntime: FATAL EXCEPTION: Thread-3
                            //sb.setProgress(0);//set to zero,invalidate
                            mp.release();
                            positionPlay=((positionPlay+1)%mySongsPlay.size()); // список треков
                            Uri u = Uri.parse(mySongsPlay.get( positionPlay).toString()); //перейти к конкретному треку
                            mp = MediaPlayer.create(context,u);

                            //sb.setProgress(0);//set to zero,invalidate
                            mp.start();

                        }
                        // воспроизводится но выкидивает варнинг>>
                        //W/MediaPlayer: Couldn't open /storage/emulated/0/Download/Albir_Musiq_-_Everyday.mp3: java.io.FileNotFoundException: No content provider:

                    }
                }
            };

        } catch (Exception exception) {
            Log.d(TAG,"--Fall out Exception -- "+exception.getMessage());
        }

        //----------------------------------------------------------------------------------
        if(mp != null){
            mp.stop();
            mp.release();
            Log.d(TAG,"--STOP & release()--");

        }

        positionPlay = position;

        // может вылетать при прокручивании списка и выбра позиции !  при прстом переключениии не вылетает

        //Out Of Bounds  - За границами
        //java.lang.IndexOutOfBoundsException: Index: 12, Size: 12 - E/AndroidRuntime: FATAL EXCEPTION
        // java.lang.IndexOutOfBoundsException: Index: 12, Size: 12
        // java.lang.IndexOutOfBoundsException: Index: 20, Size: 12

        //  Exception in MessageQueue callback: handleReceiveCallback
        // 01-18 15:18:06.625 31133-31133/com.zeno.lbs.com.zeno.lbs.simplemp3player
        // E/MessageQueue-JNI: java.lang.IndexOutOfBoundsException: Index: 13, Size: 12

        //ArrayList<File> mySongsPlay;
        Uri u = Uri.parse(mySongsPlay.get(position).toString()); // java.lang.IndexOutOfBoundsException: Index: 15, Size: 12  -- Out Of Bounds  - За границами

        Log.d(TAG,"--Uri.parse -- " + u);
        // textView.setText(u.toString());
        new MainActivity().textName.setText("One "+ mySongsPlay.get(position).toString());


        mp = MediaPlayer.create(getApplicationContext(),u);
        Log.d(TAG,"--MediaPlayer.CREATE--");

        mp.start();

        new MainActivity().textTime.setText(""+mp.getDuration());

        sb.setMax(mp.getDuration());
        new MainActivity().updateSeekBar.start();

        //----------------------------------------------------------------------------------
    }
    //========================================================================


}
