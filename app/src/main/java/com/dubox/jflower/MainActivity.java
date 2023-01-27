package com.dubox.jflower;

import android.Manifest;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.dubox.jflower.libs.ClipBoardUtil;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.RequiresApi;
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
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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



        //创建SimpleAdapter适配器将数据绑定到item显示控件上
        adapter = new SimpleAdapter(
                MainActivity.this,
                deviceDataList,
                R.layout.device_item,
                new String[]{"name"},
                new int[]{R.id.device_name});
        //实现列表的显示
//        deviceListV = this.findViewById(R.id.device_list);


        Log.i("ip", localIp = localIp());

        initHandler();

        PermissonUtil.checkPermission(MainActivity.this, new PermissionListener() {
            @Override
            public void havePermission() {
//                Toast.makeText(MainActivity.this, "获取成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void requestPermissionFail() {
                Toast.makeText(MainActivity.this, "授权失败", Toast.LENGTH_SHORT).show();
            }
        }, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});

        Log.i("main", "mainmainmainmainmain");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startService();
        }


        AsyncHttpServer server = new AsyncHttpServer();

        List<WebSocket> _sockets = new ArrayList<WebSocket>();

        server.get("/", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                response.send("Hello!!!");
            }
        });

// listen on port 5000
        server.listen(8891);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void startService(){
        Context context = getApplicationContext();
        Intent intent = new Intent(this, HttpService.class); // Build the intent for the service
        context.startForegroundService(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i("onResume", "000000000000");
        deviceDetect();
        act = Act.NONE;
        handleShare();
        getClipboardData();
    }

    private void getClipboardData() {
        if(act != Act.NONE)return;
        this.getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                    act = Act.PASTE;
                    Log.i("paste", ClipBoardUtil.paste());
                    sharingType = SharingType.TEXT;
                    waitingText = ClipBoardUtil.paste();

            }
        });
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

    protected void clearDeviceList(){
        if(!deviceListCleared)return;
        deviceListCleared = false;
        HashMap<String, Object> item = new HashMap<>();
        item.put("name", "未检测到设备");

        deviceDataList.add(item);
    }

    public void deviceDetect() {
        clearDeviceList();
        if(localIp.equals("")){
            Toast.makeText(getApplicationContext(),"没有检测到本机IP，请确定已连接到局域网" , Toast.LENGTH_LONG).show();
            return;
        }
        String ipSeg = localIp.replaceFirst("\\d+$", "");
        Log.i("ipSeg", ipSeg);
        for (int i = 0; i < 256; i++) {

            String ip = ipSeg + i;
            String uri = String.format("http://%s:8891/detect", ip);

            AsyncHttpClient.getDefaultInstance().executeString(new AsyncHttpRequest(Uri.parse(uri), "GET")
                    .setTimeout(15000)
                    .setHeader("cmd", "detect")
                    .setHeader("ip", localIp)
                    .setHeader("id", localId)
                    .setHeader("name",urlEncode(localName))
                    .setHeader("findingCode", ""), new AsyncHttpClient.StringCallback() {
                // Callback is invoked with any exceptions/errors, and the result, if available.
                @Override
                public void onCompleted(Exception e, AsyncHttpResponse response, String result) {
                    //Log.i("uri1", uri);
                    if (e != null ) {
//                        if(ip.equals("192.168.1.102")){
//                            e.printStackTrace();
//                        }
                        return;
                    }
                    HashMap<String, Object> item = new HashMap<>();
                    item.put("name", ip);
                    if(!deviceListCleared){
                        deviceDataList.clear();
                        deviceListCleared = true;
                    }
//                    deviceDataList.add(item);
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
            map.put("file_name",Uri.parse(fPath).getLastPathSegment());

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
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .setHeader("Content-Length", body.length()+"")
                    .setHeader("cmd", type)
                    .setHeader("ip", localIp)
                    .setHeader("id", localId)
                    .setHeader("name",urlEncode(localName))
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
//        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        DhcpInfo info=wifiManager.getDhcpInfo();
//        System.out.println(info.serverAddress);
//        getWifiApState();
//        printHotIp();
        return getLocalIpAddress();
//        return "";
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();

                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().startsWith("192.168.")) {
                        Log.d("IPs", inetAddress.getHostAddress() );
                        Log.d("IPs", intf.getName() );
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("SocketException", ex.toString());
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
            act = Act.SHARE;
            Log.i("handleShare","111111111111");
            Log.i("type",type);
            if(Objects.equals(type, "text/plain")){

                Log.i("text/plain","ttt:"+intent.getStringExtra(Intent.EXTRA_TEXT));
                waitingText = intent.getStringExtra(Intent.EXTRA_TEXT);
                sharingType = SharingType.TEXT;
            }

// /external/images/media/269507
            // /storage/emulated/0/DCIM/Camera/IMG_20220531_101158R.jpg
//            if()
            else {
                //application/vnd.android.package-archive
                waitingImage = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                //getPathForMedia(waitingImage.toString());
//                getPathForMedia(waitingImage.getPath());
                Log.i("image/*",waitingImage.toString());
                Log.i("getPath",waitingImage.getPath());
                sharingType = SharingType.IMAGE;

            }


            //接收多张图片
            //ArrayList<Uri> uris=intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
//            if(uri!=null ){

//
//            }
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

    public void openDownloadPage(View view) {
        Uri uri = Uri.parse("https://pan.baidu.com/s/10b4SFgZnWGTO6B0BzwNtXA?pwd=tts5");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }


}