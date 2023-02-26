package com.dubox.jflower;

import android.widget.Toast;

import com.dubox.jflower.libs.ClipBoardUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MySubscriber {

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()){
            case MessageEvent.EVENT_TOAST:{
                Toast.makeText(event.getContext(), (String)event.getData(), Toast.LENGTH_LONG).show();
                break;
            }
            case MessageEvent.EVENT_COPY:{

                ClipBoardUtil.copy((String)event.getData(), event.getContext());

                break;
            }
        }
    }
}
