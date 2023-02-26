package com.dubox.jflower;

import android.content.Context;

public class MessageEvent {

    public static final int EVENT_TOAST = 1;
    public static final int EVENT_COPY = 2;
    private int type = 0;
    private Object data;

    private Context context;

    public <T> MessageEvent(Context context, int type ,T data){
        this.type = type;
        this.context = context;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public Context getContext() {
        return context;
    }

    public Object getData() {
        return data;
    }
}
