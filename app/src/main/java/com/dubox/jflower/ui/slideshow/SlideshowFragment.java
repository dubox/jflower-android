package com.dubox.jflower.ui.slideshow;

import android.content.Intent;
import android.media.VolumeShaper;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.dubox.jflower.MainActivity;
import com.dubox.jflower.R;
import com.dubox.jflower.databinding.FragmentSlideshowBinding;

import static com.dubox.jflower.ExtensionsKt.*;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private MainActivity mainActivity ;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);
        mainActivity = (MainActivity)getActivity();
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnOnClick();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public void btnOnClick() {
        binding.lockBack.setOnClickListener(v -> hideBackground(mainActivity ,true));
        binding.ignoreBattery.setOnClickListener(v -> ignoreBattery(mainActivity));
        binding.startAuto.setOnClickListener(v -> startAutostartSetting(mainActivity));
    }


}