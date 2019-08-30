package com.nehad.android.movieapp;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.design.widget.CollapsingToolbarLayout;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.nehad.android.movieapp.Adapter.ReviewAdapter;
import com.nehad.android.movieapp.Adapter.TrailerAdapter;
import com.nehad.android.movieapp.Database.AppDatabase;
import com.nehad.android.movieapp.Database.FavoriteEntry;
import com.nehad.android.movieapp.Model.API.Client;
import com.nehad.android.movieapp.Model.API.Service;
import com.nehad.android.movieapp.Model.Movie;
import com.nehad.android.movieapp.Model.Review;
import com.nehad.android.movieapp.Model.ReviewResult;
import com.nehad.android.movieapp.Model.Trailer;
import com.nehad.android.movieapp.Model.TrailerResponse;
import com.nehad.android.movieapp.ViewModel.AppExecutors;
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity {
    TextView nameOfMovie, plotSynopsis, userRating, releaseDate;
    ImageView imageView;
    private RecyclerView trailerRecyclerView;
    private TrailerAdapter adapter;
    private List<Trailer> trailerList;
    private Movie favorite;
    private final AppCompatActivity activity = DetailsActivity.this;
    private AppDatabase mDb;
    List<FavoriteEntry> entries = new ArrayList<>();
  //  boolean exists;

    Movie movie;
    String thumbnail, movieName, synopsis, rating, dateOfRelease;
    int movie_id;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mDb = AppDatabase.getInstance(getApplicationContext());

        imageView = (ImageView) findViewById(R.id.thumbnail_image_header);
        // nameOfMovie = (TextView) findViewById(R.id.title);
        plotSynopsis = (TextView) findViewById(R.id.plotSynopsis);
        userRating = (TextView) findViewById(R.id.userrating);
        releaseDate = (TextView) findViewById(R.id.releaseDate);

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity.hasExtra("movies")){

            movie = getIntent().getParcelableExtra("movies");

            thumbnail = movie.getPosterPath();
            movieName = movie.getOriginalTitle();
            synopsis = movie.getOverview();
            rating = Double.toString(movie.getVoteAverage());
            dateOfRelease = movie.getReleaseDate();
            movie_id = movie.getId();

            String poster = "https://image.tmdb.org/t/p/w500" + thumbnail;

            Glide.with(this)
                    .load(poster)
                    .placeholder(R.drawable.load)
                    .into(imageView);

            //  nameOfMovie.setText(movieName);
            plotSynopsis.setText(synopsis);
            userRating.setText(rating);
            releaseDate.setText(dateOfRelease);


            ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar)).setTitle(movieName);

        }else{
            Toast.makeText(this, "No API Data", Toast.LENGTH_SHORT).show();
        }

        checkStatus(movieName);
        initTrailer();

    }

    private void initTrailer(){
        trailerList = new ArrayList<>();
        adapter = new TrailerAdapter(this, trailerList);

        trailerRecyclerView = (RecyclerView) findViewById(R.id.trailer_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        trailerRecyclerView.setLayoutManager(mLayoutManager);
        trailerRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        MoviesTrailers();
        MovieReviews();
    }

    private void MoviesTrailers(){
        try{
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()){
                Toast.makeText(getApplicationContext(), "Please get your API Key", Toast.LENGTH_SHORT).show();
                return;
            }else {
                Client Client = new Client();
                Service apiService = Client.getClient().create(Service.class);
                Call<TrailerResponse> call = apiService.getMovieTrailer(movie_id, BuildConfig.THE_MOVIE_DB_API_TOKEN);
                call.enqueue(new Callback<TrailerResponse>() {
                    @Override
                    public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                //List<Trailer> trailer = response.body();
                                TrailerResponse trailer = response.body();

                                RecyclerView recyclerView = findViewById(R.id.trailer_recycler_view);
                                LinearLayoutManager firstManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
                                recyclerView.setLayoutManager(firstManager);
                                recyclerView.setAdapter(new TrailerAdapter(getApplicationContext(), trailer.getResults()));
                                recyclerView.smoothScrollToPosition(0);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<TrailerResponse> call, Throwable t) {
                        Log.d("Error", t.getMessage());
                        Toast.makeText(DetailsActivity.this, "Error fetching trailer", Toast.LENGTH_SHORT).show();

                    }
                });
            }

        } catch(Exception e){
            Log.d("Error", e.getMessage());
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
        }
    }


    //TODO
    private void MovieReviews(){
        try {
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please get your API Key", Toast.LENGTH_SHORT).show();
                return;
            } else {
                Client Client = new Client();
                Service apiService = Client.getClient().create(Service.class);
                Call<Review> call = apiService.getReview(movie_id, BuildConfig.THE_MOVIE_DB_API_TOKEN);

                call.enqueue(new Callback<Review>() {
                    @Override
                    public void onResponse(Call<Review> call, Response<Review> response) {
                        if (response.isSuccessful()){
                            if (response.body() != null){
                                List<ReviewResult> reviewResults = response.body().getResults();
                                RecyclerView ReviewRecyclerView = findViewById(R.id.review_recyclerview);
                                LinearLayoutManager firstManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
                                ReviewRecyclerView.setLayoutManager(firstManager);
                                ReviewRecyclerView.setAdapter(new ReviewAdapter(getApplicationContext(), reviewResults));
                                ReviewRecyclerView.smoothScrollToPosition(0);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Review> call, Throwable t) {

                    }
                });
            }

        } catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, "unable to fetch data",Toast.LENGTH_SHORT).show();
        }

    }

    public void saveFavorite(){
        Double rate = movie.getVoteAverage();
        final FavoriteEntry favoriteEntry = new FavoriteEntry(movie_id, movieName, rate, thumbnail, synopsis);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.favoriteDao().insertFavorite(favoriteEntry);
            }
        });
    }

    private void deleteFavorite(final int movie_id){
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.favoriteDao().deleteFavoriteWithId(movie_id);
            }
        });
    }



    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            bgDrawable.draw(canvas);
        }   else{
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    @SuppressLint("StaticFieldLeak")
    private void checkStatus(final String movieName){
        final MaterialFavoriteButton materialFavoriteButton = (MaterialFavoriteButton) findViewById(R.id.favorite_button);
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params){
                entries.clear();
                entries = mDb.favoriteDao().loadAll(movieName);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid){
                super.onPostExecute(aVoid);
                if (entries.size() > 0){
                    materialFavoriteButton.setFavorite(true);
                    materialFavoriteButton.setOnFavoriteChangeListener(
                            new MaterialFavoriteButton.OnFavoriteChangeListener() {
                                @Override
                                public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                                    if (favorite == true) {
                                        saveFavorite();
                                        Snackbar.make(buttonView, "Added to Favorite",
                                                Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        deleteFavorite(movie_id);
                                        Snackbar.make(buttonView, "Removed from Favorite",
                                                Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });


                }else {
                    materialFavoriteButton.setOnFavoriteChangeListener(
                            new MaterialFavoriteButton.OnFavoriteChangeListener() {
                                @Override
                                public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                                    if (favorite == true) {
                                        saveFavorite();
                                        Snackbar.make(buttonView, "Added to Favorite",
                                                Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        int movie_id = getIntent().getExtras().getInt("id");
                                        deleteFavorite(movie_id);
                                        Snackbar.make(buttonView, "Removed from Favorite",
                                                Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        }.execute();
    }
}

