package com.nehad.android.movieapp.Model.API;

import com.nehad.android.movieapp.Model.MoviesResponse;
import com.nehad.android.movieapp.Model.Review;
import com.nehad.android.movieapp.Model.TrailerResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface Service {

    @GET("/3/movie/{popular}?")
    Call<MoviesResponse>getMovies(@Path ("popular") String popular , @Query("api_key") String apikey);

//    @GET("/3/movie/{popular}?")
//    Call<MoviesResponse>getMovies(@Path ("popular") String popular , @Query("api_key") String apiKey);



    @GET("/3/movie/{movie_id}/videos")
    Call<TrailerResponse> getMovieTrailer(@Path ("movie_id") int id , @Query("api_key") String apikey);


    //Reviews
    @GET("/3/movie/{movie_id}/reviews")
    Call<Review> getReview(@Path("movie_id") int id, @Query("api_key") String apiKey);


}