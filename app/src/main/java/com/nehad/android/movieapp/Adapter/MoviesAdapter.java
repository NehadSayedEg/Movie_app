package com.nehad.android.movieapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.nehad.android.movieapp.DetailsActivity;
import com.nehad.android.movieapp.Model.Movie;
import com.nehad.android.movieapp.Model.MoviesResponse;
import com.nehad.android.movieapp.R;

import java.util.List;


public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder> {

    private Context mContext;
    private MoviesResponse movieList;
    private int flag;


    public MoviesAdapter(Context mContext, MoviesResponse movieList1, int flag) {
        this.mContext = mContext;
        this.movieList = movieList1;
        this.flag = flag;
    }

    @Override
    public MoviesAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i){
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.movie_card, viewGroup, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MoviesAdapter.MyViewHolder viewHolder, int i){
        SharedPreferences  sharedPreferences = mContext.getSharedPreferences("myPref",mContext.MODE_PRIVATE);
      SharedPreferences.Editor editor =sharedPreferences.edit();
      editor.putInt("postion",i);
      editor.apply();

        Log.v("adapterMovies", "binding");
        viewHolder.title.setText(movieList.getResults().get(i).getOriginalTitle());
        Log.v("adapterMovies", "title " +movieList.getResults().get(i).getOriginalTitle());
        String vote = Double.toString(movieList.getResults().get(i).getVoteAverage());
        viewHolder.userrating.setText(vote);

        String poster;
        if(flag == 0) {
            poster = "https://image.tmdb.org/t/p/w500" + movieList.getResults().get(i).getPosterPath();
            // poster = "https://image.tmdb.org/t/p/w500" + movieList.get(i).getPosterPath();

        }else {
            poster ="https://image.tmdb.org/t/p/w500" + movieList.getResults().get(i).getPosterPath();
        }
        Log.v("adapterMovies", "path " + poster);
        //Glide.with(mContext).load(poster).placeholder(R.drawable.load).into(viewHolder.thumbnail);
        Log.e(" getError ", poster);



        Glide.with(mContext)
                .load(poster)
                .placeholder(R.drawable.load)
                .into(viewHolder.thumbnail);

    }

    public void setMovies(List<Movie> movie) {
      // this.movieList.getResults().clear();
        this.movieList = new MoviesResponse();
        this.movieList.setResults(movie);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount(){
        if (movieList != null) {

            if (movieList.getResults() != null) {
                return movieList.getResults().size();
            }}

        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView title, userrating;
        public ImageView thumbnail;

        public MyViewHolder(View view){
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            userrating = (TextView) view.findViewById(R.id.userRating);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);

            view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION){
                        Movie clickedDataItem = movieList.getResults().get(pos);
                        Intent intent = new Intent(mContext, DetailsActivity.class);
                        intent.putExtra("movies", clickedDataItem );
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        Toast.makeText(v.getContext(), "You clicked " + clickedDataItem.getOriginalTitle(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
