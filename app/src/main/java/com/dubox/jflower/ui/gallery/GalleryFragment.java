package com.dubox.jflower.ui.gallery;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dubox.jflower.MainActivity;
import com.dubox.jflower.databinding.FragmentGalleryBinding;
import com.dubox.jflower.libs.MyCallback;
import com.dubox.jflower.libs.Utils;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.body.UrlEncodedFormBody;

import org.json.JSONException;
import org.json.JSONObject;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private MainActivity mainActivity ;
    protected GalleryViewModel galleryViewModel;
    protected PackageInfo pInfo;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);
        mainActivity = (MainActivity)getActivity();

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        galleryViewModel.getText().observe(getViewLifecycleOwner(), binding.textGallery::setText);
        galleryViewModel.getNewV().observe(getViewLifecycleOwner(), binding.newV::setText);
        galleryViewModel.getBtnShow().observe(getViewLifecycleOwner(), binding.button::setVisibility);

        this.setV();
        this.getNewV();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    protected void setV(){
        pInfo = Utils.getPackageInfo();
        if(pInfo == null)return;
        String version = pInfo.versionName;
        galleryViewModel.setV(version);
    }

    protected void getNewV(){
        Utils.getNewVersion(pInfo.versionName, new MyCallback() {
            @Override
            public void onNewVersion(JSONObject data) throws JSONException {
                    galleryViewModel.setNewV((String) data.get("buildVersion"));
                    galleryViewModel.setBtnShow((boolean)data.get("buildHaveNewVersion"));
            }
        });
    }



}