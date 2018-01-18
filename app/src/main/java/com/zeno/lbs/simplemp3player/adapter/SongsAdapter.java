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

    @Override
    public SongsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_list_row, parent, false);

        return new SongsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SongsAdapter.MyViewHolder holder, int position) {
        Song song = songsList.get(position);
        holder.title.setText(song.getTitle());
        //holder.genre.setText(song.getGenre());
        //holder.year.setText(song.getYear());
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }
}

