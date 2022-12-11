package com.dubox.jflower;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.dubox.jflower.databinding.ActivityMainBinding;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.body.FileBody;
import com.koushikdutta.async.http.body.StreamBody;
import com.koushikdutta.async.http.body.StringBody;
import com.koushikdutta.async.http.callback.HttpConnectCallback;
import com.koushikdutta.async.http.server.UnknownRequestBody;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    protected List<HashMap<String, Object>> deviceDataList = new ArrayList<>();
    protected SimpleAdapter adapter;

    protected Handler handler;

    protected String localIp = "";
    protected String localId = "";
    protected String localName = "";

    protected String[] toastMsg = new String[]{"发送失败","发送成功"};

    protected String waitingText = "";
    protected Uri waitingImage;
    protected String sharingType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceDetect();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);//

        navigationView.setNavigationItemSelectedListener(item -> {
            Log.i("nav", item.getTitle().toString());
            navController.navigate(item.getItemId());
            drawer.closeDrawer(Gravity.LEFT);
            return false;
        });

        localId = settingGet("localId",md5(System.currentTimeMillis()+""));Log.i("localId",localId);
        localName = Settings.Secure.getString(getContentResolver(), "bluetooth_name");Log.i("localName",localName);


        this.handleShare();

        Log.i("ip", localIp = localIp());

        //创建SimpleAdapter适配器将数据绑定到item显示控件上
        adapter = new SimpleAdapter(
                MainActivity.this,
                deviceDataList,
                R.layout.device_item,
                new String[]{"name"},
                new int[]{R.id.device_name});
        //实现列表的显示
        ListView deviceListV = this.findViewById(R.id.device_list);
        deviceListV.setAdapter(adapter);
        deviceListV.setOnItemClickListener((adapterView, view, i, l) -> {
            HashMap<String,Object> map =  (HashMap<String, Object>) adapterView.getItemAtPosition(i);
            switch (sharingType){
                case "Text":
                    deviceSendText(map.get("name").toString() ,waitingText);
                    break;
                case "Image":
                    deviceSendImg(map.get("name").toString() ,waitingImage);
                    break;
            }

        });
        deviceDetect();
        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        adapter.notifyDataSetChanged();
                        break;
                    case 10:
                    case 11:
                        Toast.makeText(getApplicationContext(),toastMsg[msg.what-10] , Toast.LENGTH_LONG).show();
                        break;
                }

            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    protected String settingGet(String name ,String def){
        SharedPreferences setting = getSharedPreferences("com.dubox.jflower", 0);

        String v = setting.getString(name, "");
        if(v == ""){
            setting.edit().putString(name, def).commit();
        }
        return setting.getString(name, "");
    }

    protected String settingGet(String name ){
        return settingGet(name ,"");
    }

    protected Boolean settingSet(String name ,String value ){
        SharedPreferences setting = getSharedPreferences("com.dubox.jflower", 0);
        return setting.edit().putString(name, value).commit();
    }

    protected void deviceDetect() {

        deviceDataList.clear();
        String ipSeg = localIp.replaceFirst("\\d+$", "");
        Log.i("ipSeg", ipSeg);
        for (int i = 0; i < 256; i++) {

            String ip = ipSeg + i;
            String uri = String.format("http://%s:8891/detect", ip);

            AsyncHttpClient.getDefaultInstance().executeString(new AsyncHttpRequest(Uri.parse(uri), "GET")
                    .setTimeout(7000)
                    .setHeader("cmd", "detect")
                    .setHeader("ip", localIp)
                    .setHeader("id", localId)
                    .setHeader("name",urlEncode(localName))
                    .setHeader("findingCode", ""), new AsyncHttpClient.StringCallback() {
                // Callback is invoked with any exceptions/errors, and the result, if available.
                @Override
                public void onCompleted(Exception e, AsyncHttpResponse response, String result) {
                    Log.i("uri1", uri);
                    if (e != null ) {
                        if(ip.equals("192.168.8.111")){
                            e.printStackTrace();
                        }
                        return;
                    }
                    HashMap<String, Object> item = new HashMap<>();
                    item.put("name", ip);
                    deviceDataList.add(item);
                    handler.sendEmptyMessage(1);
                    Log.i("uri", uri);
                    Log.i("response", response.headers().toString());
                    Log.i("result", result);
                }
            });
        }

    }

    public void deviceSendImg(String ip,Uri data) {

        Log.i("img",data.getPath());
        try {
            FileInputStream fis = new FileInputStream(data.getPath());Log.i("fis.available()",fis.available()+"");
            deviceSend( ip , "file",
            new StreamBody(fis  ,fis.available())
            );
        } catch (FileNotFoundException e) {
            Log.i("send","11");
            e.printStackTrace();
            handler.sendEmptyMessage(10);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void deviceSendText(String ip,String data) {

            deviceSend( ip , "text",
                    new StringBody(data)
            );
    }

    protected void deviceSend(String ip ,String type,AsyncHttpRequestBody body) {

            String uri = String.format("http://%s:8891/%s", ip ,type);

        AsyncHttpRequest req = new AsyncHttpRequest(Uri.parse(uri), "POST")
                    //.setTimeout(5000)
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .setHeader("Content-Length", body.length()+"")
                    .setHeader("cmd", type)
                    .setHeader("ip", localIp)
                    .setHeader("id", localId)
                    .setHeader("name",urlEncode(localName))
                    //.setHeader("file_name",urlEncode(localName))
                    .setHeader("findingCode", "");
        req.setBody(body);Log.i("headers",req.getHeaders().toString());

            AsyncHttpClient.getDefaultInstance().executeString(req, new AsyncHttpClient.StringCallback() {



                // Callback is invoked with any exceptions/errors, and the result, if available.
                @Override
                public void onCompleted(Exception e, AsyncHttpResponse response, String result) {

                    if (e != null) {
                        Log.i("send","22");
                        handler.sendEmptyMessage(10);
                        return;
                    }
                    Log.i("send","33");
                    handler.sendEmptyMessage(11);
                }
            });


    }


    protected String localIp() {

        ConnectivityManager connManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);//.getActiveNetworkInfo();//

        if (null != wifiInfo) {
            NetworkInfo.State state = wifiInfo.getState();
            if (null != state) {
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {

                    WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    WifiInfo connInfo = wifiManager.getConnectionInfo();
                    return formatIp(connInfo.getIpAddress());
                }
            }
        }
        return "";
    }

    protected String formatIp( int ip)
    {
        return String.format("%d.%d.%d.%d",ip&0x000000ff,(ip&0x0000ff00)>>8,(ip&0x00ff0000)>>16,(ip&0xff000000)>>24);
    }

    private void handleShare(){
        Intent intent=getIntent();
        String action=intent.getAction();
        String type=intent.getType();
        if(action.equals(Intent.ACTION_SEND)){
            Log.i("type",type);
            if(Objects.equals(type, "text/plain")){

                Log.i("text/plain","ttt:"+intent.getStringExtra(Intent.EXTRA_TEXT));
                waitingText = intent.getStringExtra(Intent.EXTRA_TEXT);
                sharingType = "Text";
            }
///external/images/media/269507
            if(Objects.equals(type, "image/*") || true){
                waitingImage = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                Log.i("image/*",waitingImage.toString());
                Log.i("getPath",waitingImage.getPath());
                sharingType = "Image";
            }


            //接收多张图片
            //ArrayList<Uri> uris=intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
//            if(uri!=null ){

//
//            }
        }
    }

    public String md5(String info)
    {
        try
        {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("UTF-8"));
            byte[] encryption = md5.digest();

            StringBuffer strBuf = new StringBuffer();
            for (int i = 0; i < encryption.length; i++)
            {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1)
                {
                    strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
                }
                else
                {
                    strBuf.append(Integer.toHexString(0xff & encryption[i]));
                }
            }

            return strBuf.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            return "";
        }
        catch (UnsupportedEncodingException e)
        {
            return "";
        }
    }

    public String urlEncode(String str){
        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

}