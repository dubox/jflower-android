package com.dubox.jflower;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.dubox.jflower.libs.ClipBoardUtil;
import com.dubox.jflower.libs.Utils;
import com.dubox.jflower.libs.utilsTrait.Net;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import permison.PermissonUtil;
import permison.listener.PermissionListener;

import com.dubox.jflower.databinding.ActivityMainBinding;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.body.StreamBody;
import com.koushikdutta.async.http.body.StringBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    public ListView deviceListV;
    protected List<HashMap<String, Object>> deviceDataList = new ArrayList<>();
    public SimpleAdapter adapter;

    protected Handler handler;

    protected enum Act {SHARE ,PASTE ,NONE;}
    protected Act act = Act.NONE;

    protected String localIp = "";
    protected String localId = "";
    protected String localName = "";

    protected String[] toastMsg = new String[]{"发送失败","发送成功"};

    public String waitingText = "";
    public Uri waitingImage;
    public enum SharingType {TEXT,IMAGE,NONE;}
    public SharingType sharingType = SharingType.NONE;
    private boolean deviceListCleared = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("onCreate", "22222222222222222");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        checkPermission();
        startService();

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

        localId = Utils.getId();Log.i("localId",localId);
        localName = Utils.getName();Log.i("localName",localName);



        //创建SimpleAdapter适配器将数据绑定到item显示控件上
        adapter = new SimpleAdapter(
                MainActivity.this,
                deviceDataList,
                R.layout.device_item,
                new String[]{"name","subName"},
                new int[]{R.id.device_name,R.id.device_sub_name}){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Button callButton = view.findViewById(R.id.openSharePage);

                // 获取数据
                HashMap<String, Object> itemData = (HashMap<String, Object>) getItem(position);
                final String ip = (String) itemData.get("subName");

                // 绑定数据到按钮
                callButton.setTag(ip);

                return view;
            }
        };


        freshIp();

        initHandler();


    }

    public void checkPermission(){
        PermissonUtil.checkPermission(this, new PermissionListener() {
            @Override
            public void havePermission() {
//                Toast.makeText(MainActivity.this, "获取成功", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void requestPermissionFail() {
                Toast.makeText(MainActivity.this, "授权失败", Toast.LENGTH_SHORT).show();
            }
        }, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});

    }


    protected void startService(){
        //启动服务
        if (!HttpService.serviceIsLive) {
            // Android 8.0使用startForegroundService在前台启动新服务
            Intent intent = new Intent(this, HttpService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        } else {
            Toast.makeText(this, "前台服务正在运行中...", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i("onResume", "000000000000");
        deviceDetect();
        act = Act.NONE;
//        handleShare();
//        getClipboardData();
        Log.i("sharingType",sharingType.toString());
    }

    public String getClipboardData() {Log.i("getClipboardData", act.toString());
        if(act == Act.SHARE)return "";

        act = Act.PASTE;
        Log.i("paste", ClipBoardUtil.paste(MainActivity.this));
//        sharingType = SharingType.TEXT;
        return ClipBoardUtil.paste(MainActivity.this);

    }

    public void setAdapter(){
        deviceListV.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    protected void initHandler(){
        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        // 在主线程中提交一个Runnable来更新适配器
                        post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                        break;
                    case 10:
                    case 11:
                        Toast.makeText(getApplicationContext(),toastMsg[msg.what-10] , Toast.LENGTH_LONG).show();
                        break;
                }

            }
        };
    }


    protected void clearDeviceList(){
        if(!deviceListCleared)return;
        deviceListCleared = false;
        HashMap<String, Object> item = new HashMap<>();
        item.put("name", "未检测到设备");
        item.put("subName", "");

        deviceDataList.add(item);
    }

    public void deviceDetect() {
        freshIp();
        clearDeviceList();
        if(localIp.equals("")){
            Toast.makeText(getApplicationContext(),"没有检测到本机IP，请确定已连接到局域网" , Toast.LENGTH_LONG).show();
            return;
        }
        String ipSeg = localIp.replaceFirst("\\d+$", "");
        Log.i("ipSeg", ipSeg);
        for (int i = 0; i < 256; i++) {

            String ip = ipSeg + i;
            if(ip.equals(localIp))continue;
            String uri = String.format("http://%s:8891/detect", ip);

            AsyncHttpClient.getDefaultInstance().executeString(new AsyncHttpRequest(Uri.parse(uri), "GET")
                    .setTimeout(15000)
                    .setHeader("cmd", "detect")
                    .setHeader("ip", localIp)
                    .setHeader("id", localId)
                    .setHeader("name",Utils.urlEncode(localName))
                    .setHeader("findingCode", ""), new AsyncHttpClient.StringCallback() {
                // Callback is invoked with any exceptions/errors, and the result, if available.
                @Override
                public void onCompleted(Exception e, AsyncHttpResponse response, String result) {
                    //Log.i("uri1", uri);
                    if (e != null ) {
                        return;
                    }
                    Log.i("uri", uri);
                    Log.i("response", response.headers().toString());
                    Log.i("result", result);
                    HashMap<String, Object> item = new HashMap<>();
                    item.put("name", Utils.urlDecode(response.headers().get("name")));
                    item.put("subName", ip);
                    if(!deviceListCleared){
                        deviceDataList.clear();
                        deviceListCleared = true;
                    }
                    deviceDataList.add(item);
                    handler.sendEmptyMessage(1);

                }
            });
        }

    }

    public void deviceSendImg(String ip,Uri data) {

        String fPath = data.getPath();
        if(fPath.startsWith("/external/")){
            fPath = getPathForMedia(data.toString());
        }
        Log.i("file",fPath);
        Log.i("getLastPathSegment",Uri.parse(fPath).getLastPathSegment());

        try {
//            FileInputStream fis = new FileInputStream(fPath);
            InputStream fis = getContentResolver().openInputStream(data);
            Log.i("fis.available()",fis.available()+"");
            HashMap<String,Object> map = new HashMap<String,Object>();
            map.put("file_name",Utils.urlEncode(Uri.parse(fPath).getLastPathSegment()));

            deviceSend( ip , "file",
            new StreamBody(fis  ,fis.available()),
                    map
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

    protected void deviceSend(String ip ,String type,AsyncHttpRequestBody body  ) {
        deviceSend(ip ,type,body,new HashMap<>());
    }

    protected void deviceSend(String ip ,String type,AsyncHttpRequestBody body ,HashMap<String, Object> options ) {

            String uri = String.format("http://%s:8891/%s", ip ,type);

        AsyncHttpRequest req = new AsyncHttpRequest(Uri.parse(uri), "POST")
                    //.setTimeout(5000)
                    .setHeader("Content-Type", "text/plain")
//                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .setHeader("Content-Length", body.length()+"")
                    .setHeader("cmd", type)
                    .setHeader("ip", localIp)
                    .setHeader("id", localId)
                    .setHeader("name",Utils.urlEncode(localName))
                    //.setHeader("file_name",urlEncode(localName))
                    .setHeader("findingCode", "");
        options.forEach((key, value) -> {
            req.setHeader(key ,value.toString());
        });
        req.setBody(body);Log.i("headers",req.getHeaders().toString());

            AsyncHttpClient.getDefaultInstance().executeString(req, new AsyncHttpClient.StringCallback() {



                // Callback is invoked with any exceptions/errors, and the result, if available.
                @Override
                public void onCompleted(Exception e, AsyncHttpResponse response, String result) {

                    if (e != null) {
                        Log.i("send","22");
                        try {
                            throw e;
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
//                        handler.sendEmptyMessage(10);
//                        return;
                    }
                    Log.i("send","33");
                    handler.sendEmptyMessage(11);
                }
            });


    }

    void freshIp(){
        Log.i("ip", localIp = Net.localIp(this));
    }

    protected void handleShare(){

        Intent intent=getIntent();
        String action=intent.getAction();
        String type=intent.getType();

        if(action != null && action.equals(Intent.ACTION_SEND)){
            act = Act.SHARE;
            Log.i("handleShare","111111111111");
            Log.i("type",type);
            Uri shareUri = (Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if( shareUri != null)
                Log.i("ShareUri",shareUri.getPath());
            if(Objects.equals(type, "text/plain") && shareUri == null){

                Log.i("text/plain","ttt:"+intent.getStringExtra(Intent.EXTRA_TEXT));
                waitingText = intent.getStringExtra(Intent.EXTRA_TEXT);
                sharingType = SharingType.TEXT;
            }

// /external/images/media/269507
            // /storage/emulated/0/DCIM/Camera/IMG_20220531_101158R.jpg
//            if()
            else {
                //application/vnd.android.package-archive
                waitingImage = shareUri;//intent.getParcelableExtra(Intent.EXTRA_STREAM);
                //getPathForMedia(waitingImage.toString());
//                getPathForMedia(waitingImage.getPath());
                Log.i("image/*",waitingImage.toString());

                sharingType = SharingType.IMAGE;

            }


            //接收多张图片
            //ArrayList<Uri> uris=intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
//            if(uri!=null ){

//
//            }
        }else {
            sharingType = SharingType.NONE;
        }
    }


    public String getPathForMedia(String meidaUrl){
        Uri uri = Uri.parse(meidaUrl);

        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor actualimagecursor = getContentResolver().query(uri,proj,null,null,null);
        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        actualimagecursor.moveToFirst();

        String img_path = actualimagecursor.getString(actual_image_column_index);
Log.i("img_path",img_path);
        return img_path;
//        File file = new File(img_path);
//
//        Uri fileUri = Uri.fromFile(file);
//        Log.i("fileUri",fileUri.toString());
    }




    public void openDownloadPage(View view) {
        Uri uri = Uri.parse("https://www.pgyer.com/jflower");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void openSharePage(View view) {
        String ip = (String)view.getTag();
        Uri uri = Uri.parse("http://"+ip+":8891/share");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public String getLocalIp(){
        if(localIp.equals("")){
            freshIp();
        }
        return localIp;
    }


}