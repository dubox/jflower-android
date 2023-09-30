package com.dubox.jflower.libs;

import org.json.JSONException;
import org.json.JSONObject;

public interface MyCallback {
    void onNewVersion(JSONObject data) throws JSONException;
}
