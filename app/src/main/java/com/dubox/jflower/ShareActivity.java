package com.dubox.jflower;

import android.content.Intent;
import android.util.Log;

public class ShareActivity extends MainActivity {

    @Override
    protected void onResume() {
        super.onResume();
        handleShare();
        Log.i("sharingType", sharingType.toString());
    }

    @Override
    protected void onStop() {
        super.onStop();
        startActivity(new Intent(this, MainActivity.class));
    }
}
