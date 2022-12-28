package com.dubox.jflower.ui.gallery;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;

import com.dubox.jflower.MainActivity;
import com.dubox.jflower.R;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;

import org.json.JSONObject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import static com.google.android.material.internal.ContextUtils.getActivity;

public class GalleryViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<String> newVText;
    private final MutableLiveData<Integer> showUpBtn;

    public GalleryViewModel() {
        mText = new MutableLiveData<>();
        newVText = new MutableLiveData<>();
        showUpBtn = new MutableLiveData<>();

        mText.setValue("Current version: " );
        newVText.setValue("New version: " );
//        showUpBtn.setValue(0);
    }

    public void setV(String v){
        mText.setValue("Current version: "+ v);
    }
    public void setNewV(String v){
        newVText.postValue("New version: "+ v);
    }
    public void setBtnShow(int show){
        showUpBtn.postValue( show==0? View.INVISIBLE:View.VISIBLE);
    }

    public LiveData<Integer> getBtnShow() {
        return showUpBtn;
    }
    public LiveData<String> getText() {
        return mText;
    }
    public LiveData<String> getNewV() {
        return newVText;
    }
}