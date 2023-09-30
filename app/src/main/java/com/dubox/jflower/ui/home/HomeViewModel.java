package com.dubox.jflower.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> myIp;

    public HomeViewModel() {
        myIp = new MutableLiveData<>();
        myIp.setValue("Serving At: 192.168.?.?");
    }

    public LiveData<String> getMyIp() {
        return myIp;
    }

    public void setMyIp(String v){
        myIp.setValue("Serving At: "+ v);
    }
}