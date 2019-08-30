package com.nehad.android.movieapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v4.widget.SwipeRefreshLayout;

import com.nehad.android.movieapp.Adapter.MoviesAdapter;
import com.nehad.android.movieapp.Database.FavoriteEntry;
import com.nehad.android.movieapp.Model.API.Client;
import com.nehad.android.movieapp.Model.API.Service;
import com.nehad.android.movieapp.Model.Movie;
import com.nehad.android.movieapp.Model.MoviesResponse;
import com.nehad.android.movieapp.ViewModel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class MainActivity extends AppCompatActivity  {

    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    ProgressDialog progressDialog;
    private MoviesResponse movieList;

    private AppCompatActivity activity = MainActivity.this;
    public static final String LOG_TAG = MoviesAdapter.class.getName();

    private static String LIST_STATE = "list_state";
    private Parcelable savedRecyclerLayoutState;
    private static final String BUNDLE_RECYCLER_LAYOUT = "recycler_layout";
    private ArrayList<Movie> moviesInstance = new ArrayList<>();
    public SwipeRefreshLayout swipeContainer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }

        swipeContainer = findViewById(R.id.main_content);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Movies....");
        progressDialog.setCancelable(false);


        if (savedInstanceState != null){
            moviesInstance = savedInstanceState.getParcelableArrayList(LIST_STATE);
            savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            if (savedRecyclerLayoutState != null) {
                recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
            }

            MoviesResponse movieList = new MoviesResponse();
            movieList.setResults(moviesInstance);
            recyclerView.invalidate();
            recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(),movieList,0));
            recyclerView.smoothScrollToPosition(0);
        }else {
            //progressDialog.show();
            getMovies("popular");
        }
    }

    public void getFavouriteMovies(){



        getAllFavorite();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putParcelableArrayList(LIST_STATE, moviesInstance);
        savedInstanceState.putParcelable(BUNDLE_RECYCLER_LAYOUT, recyclerView.getLayoutManager().onSaveInstanceState());
        SharedPreferences  sharedPreferences = getSharedPreferences("myPref",Context.MODE_PRIVATE);

        int pos = sharedPreferences.getInt("postion",0);

        super.onSaveInstanceState(savedInstanceState);
    }




    public Activity getActivity(){
        Context context = this;
        while (context instanceof ContextWrapper){
            if (context instanceof Activity){
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;

    }




    private void getMovies(String kind) {


        Client client = new Client();
        Service apiService = client.getClient().create(Service.class);

        Call<MoviesResponse> call = apiService.getMovies(kind, BuildConfig.THE_MOVIE_DB_API_TOKEN);
        call.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                MoviesResponse movies = response.body();
                //moviesInstance.addAll(movies);

                Log.v("Responsebody$$$$$$$$", response.toString());
//            Log.v("movies*********", "get my title" + movies.getResults().get(0).getTitle());
                moviesInstance = (ArrayList<Movie>) movies.getResults();
                recyclerView.invalidate();
                recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(),movies,0));
                recyclerView.smoothScrollToPosition(0);
                if (swipeContainer.isRefreshing()) {
                    swipeContainer.setRefreshing(false);

                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.e("Error@@@@@@@@", t.getMessage(), t);
            }
        });


    }


    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.SortByPopularity:
                getMovies("popular");
                break;
            case R.id.SortByHighratings:
                getMovies("top_rated");
                break;
            case R.id.SortByFavourite:
                getFavouriteMovies();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }


    private void getAllFavorite(){

        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getFavorite().observe(this, new Observer<List<FavoriteEntry>>() {
            @Override
            public void onChanged(@Nullable List<FavoriteEntry> imageEntries) {
                List<Movie> movies = new ArrayList<>();
                for (FavoriteEntry entry : imageEntries){
                    Movie movie = new Movie();
                    movie.setId(entry.getMovieid());
                    movie.setOverview(entry.getOverview());
                    movie.setOriginalTitle(entry.getTitle());
                    movie.setPosterPath(entry.getPosterpath());
                    movie.setVoteAverage(entry.getUserrating());

                    movies.add(movie);
                }

                MoviesResponse movieList = new MoviesResponse();
                movieList.setResults(movies);
                moviesInstance = (ArrayList<Movie>) movies;
                recyclerView.invalidate();
                recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(),movieList,0));
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }
}
