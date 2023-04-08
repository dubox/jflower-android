package com.dubox.jflower.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dubox.jflower.MainActivity;
import com.dubox.jflower.R;
import com.dubox.jflower.databinding.FragmentHomeBinding;

import java.util.HashMap;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private MainActivity mainActivity ;

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