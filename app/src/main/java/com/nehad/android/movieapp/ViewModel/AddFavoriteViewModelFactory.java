package com.nehad.android.movieapp.ViewModel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.nehad.android.movieapp.Database.AppDatabase;



public class AddFavoriteViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final int mFavoriteId;

    public AddFavoriteViewModelFactory(AppDatabase database, int favoriteId) {
        mDb = database;
        mFavoriteId = favoriteId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new AddFavoriteViewModel(mDb, mFavoriteId);
    }
}
