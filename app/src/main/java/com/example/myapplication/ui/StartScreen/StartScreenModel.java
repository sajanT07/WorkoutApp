package com.example.myapplication.ui.StartScreen;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StartScreenModel extends ViewModel {
    private final MutableLiveData<String> mText;
    private final MutableLiveData<String> bText;
    public StartScreenModel() {
        bText = new MutableLiveData<>();
        mText = new MutableLiveData<>();
        mText.setValue("WorkUP!");
        bText.setValue("Enter");
    }

    public LiveData<String> getText() {
        return mText;
    }
    public LiveData<String> getButton() {
        return bText;
    }
}
