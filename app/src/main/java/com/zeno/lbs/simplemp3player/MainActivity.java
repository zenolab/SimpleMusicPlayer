package com.zeno.lbs.simplemp3player;

//com.zeno.lbs.simplemp3player
// http://www.androidhive.info/2016/01/android-working-with-recycler-view/
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
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

import com.zeno.lbs.simplemp3player.adapter.DividerItemDecoration;
import com.zeno.lbs.simplemp3player.adapter.SongsAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // private List<Movie> movieList = new ArrayList<>();
    private List<Song> songList = new ArrayList<>();
    private RecyclerView recyclerView;
   // private MoviesAdapter mAdapter;
    private SongsAdapter mAdapter;

    //------------------------------
    public static final String LOG_TAG = "Tunes_log";


    Boolean mExternalStorageAvailable,permission=false;
    String[] items;//to read all files
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 99;

    //===============================================
    static MediaPlayer mp;//assigning memory loc once or else multiple songs will play at once
    int positionPlay;
    SeekBar sb;
    ArrayList<File> mySongsPlay;
    Thread updateSeekBar;
    Button pause,forward,reverse,next,previous;

    TextView textView;
    TextView textName;
    TextView textTime;
    //-----------------------------------------------
    //------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //eException: This Activity already has an action bar supplied by the window decor.
        // Do not request Window.FEATURE_SUPPORT_ACTION_BAR and set windowActionBar to false in your theme to use a Toolbar instead.
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

        //mAdapter = new MoviesAdapter(movieList);
        mAdapter = new SongsAdapter(songList);

        //errror - java.lang.NullPointerException

        /*
        void setHasFixedSize (boolean hasFixedSize) - это уаиличения производителности

        RecyclerView может выполнять несколько оптимизаций, если он может заранее знать,
        что размер RecyclerView в не зависит от содержимого адаптера.
         RecyclerView все еще может изменить его размер на основе других факторов (например, его родителя размер),
         но этот расчет размера не может зависеть
         от размера своих детей или содержание его адаптера (за исключением количества элементов в адаптер).

         Если использование RecyclerView попадает в эту категорию, установить это правда.
          Это позволит RecyclerView избежать недействительности весь макет при изменении его адаптера содержание.
         */
       // recyclerView.setHasFixedSize(false);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {

                chosenSong(position);

                //##########################################################
            }

            @Override
            public void onLongClick(View view, int position) {

                Toast.makeText(getApplicationContext(),  " LongClick !!!", Toast.LENGTH_SHORT).show();


            }
        }));




        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = checkStoragePermission();
        }
        else

            loadList();
        if(permission){

            loadList();
        }
        //========================================
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
         controlPanel();

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
    }

    //------------------------------------------------
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }



    //===================mp3===========================

    public ArrayList<File> findSong(File root){
        Log.d(LOG_TAG," --- findSong() --");
        ArrayList<File> at = new ArrayList<File>();
        File[] files = root.listFiles();

        for(File singleFile : files){
            if(singleFile.isDirectory() && !singleFile.isHidden()){
                at.addAll(findSong(singleFile));
            }
            else{
                if(singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")){
                    at.add(singleFile);
                }
            }
        }
        return at;
    }



    void checkExternalStorage(){
        Log.d(LOG_TAG," --- checkExternalStorage() --");
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Log.d(LOG_TAG," --- mExternalStorageAvailable = True --");
            mExternalStorageAvailable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Log.d(LOG_TAG," --- mExternalStorageAvailable = True --");
            mExternalStorageAvailable = true;
        } else {
            mExternalStorageAvailable= false;
            Log.d(LOG_TAG," --- mExternalStorageAvailable = FALSE --");
        }
        handleExternalStorageState();

    }
    void handleExternalStorageState() {
        if(mExternalStorageAvailable){
           // displayList();

            loadList();

        }
        else{
            Toast.makeText(getApplicationContext(),"Please insert an SDcard",Toast.LENGTH_LONG).show();
        }
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
                        checkExternalStorage();
                    }
                } else {
                    Toast.makeText(this, "permission denied",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    //=================================================

    void loadList(){

      //  void displayList(){
            Log.d(LOG_TAG," --- displayList() --");
            final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());
            mySongsPlay = mySongs;
            items = new String[ mySongs.size() ];
            Song song;

            for(int i=0;i<mySongs.size();i++){

                //toast(mySongs.get(i).getName().toString());
                //-- without type file
                // items[i] = mySongs.get(i).getName().toString().replace(".mp3","").replace(".wav","");
                song = new Song (items[i] = mySongs.get(i).getName().toString());
                songList.add(song);
            }



            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // probabaly not needs , сlarefy
            //Сообщите зарегистрированным наблюдателям, что набор данных изменился.
            mAdapter.notifyDataSetChanged();// - Notify any registered observers that the data set has changed. =
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


          //  ArrayAdapter<String> adp = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items);



          //  listView.setAdapter(adp);


    }

    void chosenSong(int position){


        Song song = songList.get(position);
        Toast.makeText(getApplicationContext(), song.getTitle() + " is selected!", Toast.LENGTH_SHORT).show();

        // все равно может вылелеть если клатцать очень-очень быстро преключение треков,но относитльно вполне годится -- но пака что не вылетает!
        try {

            updateSeekBar = new Thread() {


                @Override
                public void run() {
                    Log.d(LOG_TAG,"--####### updateSeekBar--########");
                    int runtime = mp.getDuration();
                    int currentPosition = 0;
                    int adv = 0;
                    // делать действие если есть более 2 миллисекунд - трека
                    while ((adv = ((adv = runtime - currentPosition) < 500) ? adv : 500) > 2) {
                        // while ((adv = ((adv = runtime - currentPosition) < 100) ? adv : 100) > 2) {
                        try {
                            currentPosition = mp.getCurrentPosition();
                            if (sb != null) {
                                sb.setProgress(currentPosition);
                            }
                            sleep(adv);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (IllegalStateException e) {
                            sb.setProgress(runtime);
                            break;
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

        Log.d(LOG_TAG,"--Uri.parse -- " + u);
        // textView.setText(u.toString());
        textName.setText("One "+ mySongsPlay.get(position).toString());


        mp = MediaPlayer.create(getApplicationContext(),u);
        Log.d(LOG_TAG,"--MediaPlayer.CREATE--");

        mp.start();

        textTime.setText(""+mp.getDuration());

        sb.setMax(mp.getDuration());
        updateSeekBar.start();

        //----------------------------------------------------------------------------------



    }

    private void controlPanel() {
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
                    pause.setText(">");
                    mp.pause();
                }
                else {
                    pause.setText("||");
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
                mp = MediaPlayer.create(getApplicationContext(),u);
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
                mp = MediaPlayer.create(getApplicationContext(),u);
                mp.start();
            }
        });
        //---------------------------------------------------------------------------
    }


    //----------------------------------------------------------------------------------------------
    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private MainActivity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final MainActivity.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

}