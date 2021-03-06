package com.zeno.lbs.simplemp3player.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zeno.lbs.simplemp3player.R;
import com.zeno.lbs.simplemp3player.Song;

import java.util.List;

/**
 * Created by grd on 1/18/18.
 */

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.MyViewHolder> {

private List<Song> songsList;


    // класс view holder-а с помощью которого мы получаем ссылку на каждый элемент
    // отдельного пункта списка
public class MyViewHolder extends RecyclerView.ViewHolder {
    public TextView title, year, genre;

    public MyViewHolder(View view) {
        super(view);
        title = (TextView) view.findViewById(R.id.title);
       // genre = (TextView) view.findViewById(R.id.genre);
      //  year = (TextView) view.findViewById(R.id.year);
    }
}


    public SongsAdapter(List<Song> songsList) {
        this.songsList = songsList;
    }

    // Создает новые views (вызывается layout manager-ом)
    @Override
    public SongsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_list_row, parent, false);

        // тут можно программно менять атрибуты лэйаута (size, margins, paddings и др.)
        return new SongsAdapter.MyViewHolder(itemView);
    }

    // Заменяет контент отдельного view (вызывается layout manager-ом)
    @Override
    public void onBindViewHolder(SongsAdapter.MyViewHolder holder, int position) {
        Song song = songsList.get(position);
        holder.title.setText(song.getTitle());
        //holder.genre.setText(song.getGenre());
        //holder.year.setText(song.getYear());
    }

    // Возвращает размер данных (вызывается layout manager-ом)
    @Override
    public int getItemCount() {
        return songsList.size();
    }
}

