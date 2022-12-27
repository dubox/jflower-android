package com.dubox.jflower.ui.gallery;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dubox.jflower.MainActivity;
import com.dubox.jflower.databinding.FragmentGalleryBinding;

import static com.google.android.material.internal.ContextUtils.getActivity;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private MainActivity mainActivity ;
    protected GalleryViewModel galleryViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);
        mainActivity = (MainActivity)getActivity();

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textGallery;
        final TextView newV = binding.newV;
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        galleryViewModel.getNewV().observe(getViewLifecycleOwner(), newV::setText);

        this.setV();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    protected void setV(){
        PackageManager manager = mainActivity.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(mainActivity.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = info.versionName;
        galleryViewModel.setV(version);
    }
}