package com.dubox.jflower.ui.home;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dubox.jflower.MainActivity;
import com.dubox.jflower.R;
import com.dubox.jflower.databinding.FragmentHomeBinding;
import com.dubox.jflower.libs.ClipBoardUtil;
import com.dubox.jflower.libs.Utils;

import java.util.HashMap;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private MainActivity mainActivity ;
    private ColorMatrixColorFilter grayFilter = Utils.getGrayFilter() ;
    int gray;
    int theme2_1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Log.i("home","sssassssss");
//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        mainActivity = (MainActivity)getActivity();
        gray = getResources().getColor(R.color.gray , mainActivity.getTheme());
        theme2_1 = getResources().getColor(R.color.theme2_1 , mainActivity.getTheme());
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.deviceDetect();
            }
        });

        initShareFab();
        binding.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchShareFab();
            }
        });
    }

    public void initShareFab(){
        if(Utils.getStorageShare(mainActivity).equals("1")){
            binding.share.setBackgroundTintList(ColorStateList.valueOf(theme2_1));
        }else {
            binding.share.setBackgroundTintList(ColorStateList.valueOf(gray));
        }
    }

    public void switchShareFab(){
        if(Utils.getStorageShare(mainActivity).equals("1")){
            Utils.setStorageShare("0");
            binding.share.setBackgroundTintList(ColorStateList.valueOf(gray));
            Toast.makeText(mainActivity, "手机目录分享关闭！", Toast.LENGTH_SHORT).show();
        }else {
            Utils.setStorageShare("1");
            binding.share.setBackgroundTintList(ColorStateList.valueOf(theme2_1));
            ClipBoardUtil.copy("http://"+mainActivity.getLocalIp()+":8891/share");
            Toast.makeText(mainActivity, "手机目录分享开启！链接已复制到剪贴板。\n(安全起见，使用后及时关闭)", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        ListView deviceListV = ((ListView)binding.getRoot().findViewById(R.id.device_list));
        deviceListV.setAdapter(mainActivity.adapter);
        deviceListV.setOnItemClickListener((adapterView, view, i, l) -> {
            HashMap<String,Object> map =  (HashMap<String, Object>) adapterView.getItemAtPosition(i);
            switch (mainActivity.sharingType){
                case TEXT:
                    mainActivity.deviceSendText(map.get("subName").toString() ,mainActivity.waitingText);
                    break;
                case IMAGE:
                    mainActivity.deviceSendImg(map.get("subName").toString() ,mainActivity.waitingImage);
                    break;
                default:
                    mainActivity.deviceSendText(map.get("subName").toString() ,mainActivity.getClipboardData());
            }

        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.i("home Destroy","sssassssss");
    }


}