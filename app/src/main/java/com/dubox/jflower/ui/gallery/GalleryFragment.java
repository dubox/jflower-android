package com.dubox.jflower.ui.gallery;

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
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;

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

        final TextView textView = binding.textGallery;
        final TextView newV = binding.newV;
        final Button upBtn = binding.button;
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        galleryViewModel.getNewV().observe(getViewLifecycleOwner(), newV::setText);
        galleryViewModel.getBtnShow().observe(getViewLifecycleOwner(), upBtn::setVisibility);

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
        PackageManager manager = mainActivity.getPackageManager();

        try {
            pInfo = manager.getPackageInfo(mainActivity.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;
        galleryViewModel.setV(version);
    }

    protected void getNewV(){
        //
//        System.out.println(View.);
        AsyncHttpClient.getDefaultInstance().executeJSONObject(new AsyncHttpRequest(
                Uri.parse(
//                        "https://gitee.com/dubox/jflower-android/raw/master/version.json"
//                        "https://raw.githubusercontent.com/dubox/jflower-android/master/version.json"
                        "https://dubox.github.io/jflower-android/version.json"
                ), "GET"), new AsyncHttpClient.JSONObjectCallback() {

            // Callback is invoked with any exceptions/errors, and the result, if available.
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse response, JSONObject result) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Looper.prepare();
                        if (e != null) {
                            System.out.println(e.toString());
                            //e.printStackTrace();
                            Toast.makeText(getContext(),"??????????????????????????????",Toast.LENGTH_LONG).show();
                            return;
                        }
                        System.out.println("I got a JSONObject: " + result);
                        try {
                            galleryViewModel.setNewV(result.get("name").toString());
                            galleryViewModel.setBtnShow((int)result.get("code") - pInfo.versionCode);
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                        Looper.loop();
                    }
                }).start();

            }
        });
    }
}