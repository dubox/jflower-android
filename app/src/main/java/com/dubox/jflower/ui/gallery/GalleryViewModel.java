package com.dubox.jflower.ui.gallery;

import android.content.pm.PackageManager;

import com.dubox.jflower.MainActivity;
import com.dubox.jflower.R;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import static com.google.android.material.internal.ContextUtils.getActivity;

public class GalleryViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<String> newVText;

    public GalleryViewModel() {
        mText = new MutableLiveData<>();
        newVText = new MutableLiveData<>();

        mText.setValue("Current version: " );
        newVText.setValue("New version: " );
    }

    public void setV(String v){
        mText.setValue("Current version: "+ v);
    }
    public void setNewV(String v){
        newVText.setValue("New version: "+ v);
    }

    public LiveData<String> getText() {
        return mText;
    }
    public LiveData<String> getNewV() {
        return newVText;
    }
}