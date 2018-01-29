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
    // private List<Movie> movieList = new ArrayList<>();
   // private List<Song> songList = new ArrayList<>();
    private RecyclerView recyclerView;
   // private MoviesAdapter mAdapter;
    private SongsAdapter mAdapter;

    Context context;

    //------------------------------
    public static final String LOG_TAG = "Tunes_log";

   // Boolean mExternalStorageAvailable,permission=false;
   // String[] items;//to read all files
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 99;

    //===============================================
   // static MediaPlayer mp;//assigning memory loc once or else multiple songs will play at once
   // int positionPlay;
    //SeekBar sb;
   // ArrayList<File> mySongsPlay;
    Thread updateSeekBar;
   // Button pause,forward,reverse,next,previous;

    TextView textView;
    TextView textName;
    TextView textTime;
    //-----------------------------------------------
    //------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

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

        mAdapter = new SongsAdapter(songList);


        // если мы уверены, что изменения в контенте не изменят размер layout-а RecyclerView
        // передаем параметр true - это увеличивает производительность
        /*
        void setHasFixedSize (boolean hasFixedSize) - это увеличения производителности

        RecyclerView может выполнять несколько оптимизаций, если он может заранее знать,
        что размер RecyclerView в не зависит от содержимого адаптера.
         RecyclerView все еще может изменить его размер на основе других факторов (например, его родителя размер),
         но этот расчет размера не может зависеть
         от размера своих детей или содержание его адаптера (за исключением количества элементов в адаптер).

         Если использование RecyclerView попадает в эту категорию, установить это правда.
          Это позволит RecyclerView избежать недействительности весь макет при изменении его адаптера содержание.

          Можно использовать android:layout_height="wrap_content" в RecyclerView,
          который, среди прочего, позволяет CollapsingToolbarLayout знать,
          что он не должен рушиться, когда RecyclerView пуст.
          Это работает только при использовании setHasFixedSize(false) в RecylcerView.

          Если вы используете setHasFixedSize(true) в RecyclerView, это поведение,
          чтобы предотвратить setHasFixedSize(true) CollapsingToolbarLayout, не работает,
          хотя RecyclerView действительно пуст.

         */
        //SetHasFixedSize (true) означает, что RecyclerView имеет дочерние элементы (элементы) с фиксированной шириной и высотой.
        // Это позволяет оптимизировать RecyclerView,
        // вычисляя точную высоту и ширину всего списка на основе вашего адаптера.
        // setHasFixedSize относится к самому RecyclerView,
        // а не к размеру каждого адаптированного к нему элемента.!
       // recyclerView.setHasFixedSize(false);
        recyclerView.setHasFixedSize(true); //SetHasFixedSize (true) означает, что RecyclerView имеет дочерние элементы (элементы) с фиксированной шириной и высотой. Это позволяет оптимизировать RecyclerView, вычисляя точную высоту и ширину всего списка на основе вашего адаптера.
        // используем linear layout manager - управляет и устанавливает планировкой экрана
        // RecyclerView - он ничего не знает о расположении элементов внутри себя. Эта работа делегирована его LayoutManage
        // есть 3 стандартных менеджера для создания списка (
        // -LinearLayoutManager для списков как в ListView,
        // -GridLayoutManager для плиток,сеток или таблиц
        // - и StaggeredGridLayoutManager для лэйаута как в Google+
        // и можно делать custom
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

        // все равно может вылелеть если клатцать очень-очень быстро преключение треков,но относитльно вполне годится -- но пака что не вылетает!
        try {

            //!! НЕПРАВИЛЬНО СЧИТАЕТСЯ progressBar
            updateSeekBar = new Thread() {


                @Override
                public void run() {
                    Log.d(LOG_TAG,"--####### updateSeekBar--########");
                    int runtime = mp.getDuration();
                    int currentPosition = 0;
                    // advance - продвижение
                    int advance = 0; // progress
                    // делать действие если есть более 2 миллисекунд - трека

                    // логическоеУсловие ? выражение1 : выражение2
                    // Если логическоеУсловие равно true, то вычисляется выражение1 и его результат становится результатом выполнения всего оператора.
                    // Если же логическоеУсловие равно false, то вычисляется выражение2, и его значение становится результатом работы оператора.
                    while ((advance = ((advance = runtime - currentPosition) < 500) ? advance : 500) > 2) {
                        // while ((advance = ((advance = runtime - currentPosition) < 100) ? advance : 100) > 2) {
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

                        // из-за того что время текущего может быть минусовое трэе может заранее перейти на следующий
                        // понять причину почему текщее время может быть с отрецательным интервалом
                        if(remain <= 0){
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



    //------------------------------------------------
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    //----------------------------------------------------------------------------------------------

    //Using in  Recycler View list for onClick and onLongClick
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

        //Intercept-перехват
        // gesture - жест
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