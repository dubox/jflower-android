package com.dubox.jflower;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.dubox.jflower.libs.ClipBoardUtil;
import com.dubox.jflower.libs.Utils;
import com.dubox.jflower.libs.utilsTrait.Net;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.body.UrlEncodedFormBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.ThemeUtils;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

public class HttpService extends Service{

    private static final int NOTIFICATION_ID = 1;
    AsyncHttpServer server = new AsyncHttpServer();

    NotificationCompat.Builder builder;

    NotificationManager notificationManager;

    Uri receivedImg;

    static boolean serviceIsLive = false;

    private static final String SHARE_ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();

    MediaPlayer mediaplayer=null;


    public HttpService() {
        EventBus.getDefault().register(new MySubscriber());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void playAudio(){
        if(mediaplayer==null){
            new Thread(new Runnable(){
                @Override
                public void run() {
                        mediaplayer=MediaPlayer.create(HttpService.this, R.raw.muted);
                        mediaplayer.setLooping(true);
//                        mediaplayer.prepareAsync ();//异步准备播放 这部必须设置不然无法播放
                    mediaplayer.start();
                }
            }).start();

        }
    }



    @Override
    public void onCreate() {
        super.onCreate();
        startServer();
        playAudio();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // If the notification supports a direct reply action, use
        // PendingIntent.FLAG_MUTABLE instead.
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);


        builder = new NotificationCompat.Builder(this, createNotificationChannel("h1", "http-service"))
                .setContentTitle("jFlower")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
//                .addAction(defAction())
                .setTicker("ticker_text");

        Log.i("notification", "ok");
        // Notification ID cannot be 0.


        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // If we get killed, after returning from here, restart
//        return super.onStartCommand(intent,flags,startId);
        serviceIsLive = true;
        startForeground(NOTIFICATION_ID, builder.build());
        return START_STICKY;
    }



    public Uri uriToPrivate(Uri uri){
        File imageFile = null;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            byte[] imageBytes = IOUtils.toByteArray(inputStream);
            // 将图片保存到应用私有目录中
            imageFile = new File(getExternalFilesDir(null), "jFlower-"+(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))+".png");
            FileOutputStream fos = null;

            fos = new FileOutputStream(imageFile);
            fos.write(imageBytes);
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(imageFile == null) {
            Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
            throw new RuntimeException();
        }
        // 获取保存后的图片的Uri
       return FileProvider.getUriForFile(this, "com.dubox.jflower.fileprovider", imageFile);

    }

    public PendingIntent shareImg( Uri uri){
        Log.i("Share",uri.toString());

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriToPrivate(uri));
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return PendingIntent.getActivity(this, 0, Intent.createChooser(shareIntent, "Share"), PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public PendingIntent shareText(String text){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        return PendingIntent.getActivity(this, 0, Intent.createChooser(shareIntent, "Share"), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private NotificationCompat.Action imgAction(Uri uri) {
        PendingIntent pendingIntent = shareImg( uri);
        return new NotificationCompat.Action.Builder(
                R.drawable.ic_menu_camera,  // 图标
                "分享",  // 操作按钮标题
                pendingIntent  // 点击操作按钮时触发的 PendingIntent
        ).build();
    }

    private NotificationCompat.Action fileAction(String ip,String key,String fileName,String serverName) {
        // 创建下载操作的 PendingIntent
        Intent downloadIntent = new Intent(this, DownloadService.class);
        downloadIntent.putExtra("fileUrl", "http://"+ip+":8891/getFile");
        downloadIntent.putExtra("ip", ip);
        downloadIntent.putExtra("serverName", serverName);
        downloadIntent.putExtra("key", key);
        downloadIntent.putExtra("fileName", fileName);

        PendingIntent downloadPendingIntent = PendingIntent.getService(
                this,
                0,
                downloadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

// 创建通知操作
       return new NotificationCompat.Action.Builder(
                R.drawable.ic_menu_slideshow,
                "接收",
                downloadPendingIntent
        ).build();
    }


    private NotificationCompat.Action textAction(String text) {
        PendingIntent pendingIntent = shareText( text);
        return new NotificationCompat.Action.Builder(
                R.drawable.ic_menu_camera,  // 图标
                "分享",  // 操作按钮标题
                pendingIntent  // 点击操作按钮时触发的 PendingIntent
        ).build();
    }
    private NotificationCompat.Action urlAction(String link) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Action.Builder(
                R.drawable.ic_menu_camera,  // 图标
                "打开链接",  // 操作按钮标题
                pendingIntent  // 点击操作按钮时触发的 PendingIntent
        ).build();
    }

    /**
     * 创建通知通道
     *
     * @param channelId
     * @param channelName
     * @return
     */
    private String createNotificationChannel(String channelId, String channelName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            service.createNotificationChannel(chan);
            Log.i("createNotificationChannel", "ok");
        }
        return channelId;
    }

    @Override
    public void onDestroy() {
        Log.e("HttpService", "onDestroy");
        serviceIsLive = false;
        stopServer();
        // 移除通知
        stopForeground(true);
        EventBus.getDefault().unregister(new MySubscriber());
        //音频
        mediaplayer.stop();
        mediaplayer=null;
        super.onDestroy();
    }

    private boolean checkRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response){
        Log.i("host",request.getHeaders().get("host"));
        if( Utils.isValidIpAddress(request.getHeaders().get("host")))return true;
        response.code(500);
        response.send("Bad request!");
        return false;
    }

    private void startServer() {
        // Create and start your HTTP server here


        server.get("/.*", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {

                if(!checkRequest(request ,response))return;

                if (request.getPath().equals("/detect")) {
                    response.getHeaders().set("id", Utils.getId());
                    try {
                        response.getHeaders().set("name", URLEncoder.encode( Utils.getName(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        response.getHeaders().set("name", Utils.getName() );
                    }
                }
                if(request.getPath().startsWith("/share")){
                    if(Utils.getStorageShare(HttpService.this) != "1"){
                        response.code(501);
                        response.send("Bad request!");
                        return;
                    }
                    String uri = Utils.removeStart(Utils.removeStart(request.getPath(), "/share"),"/");
                    String filePath = SHARE_ROOT_DIR + File.separator + uri;
                    File file = new File(filePath);
                    if (file.isDirectory()) { // 如果是目录，列出所有文件并返回
                        file.setReadable(true);
                        StringBuilder sb = new StringBuilder();
                        sb.append("<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head><body><ul>");
                        List<File> files = Arrays.asList(file.listFiles(new FileFilter() {
                            @Override
                            public boolean accept(File file) {
                                Log.i("file:", file.getName());
                                return true;
                            }
                        }));
                        files.sort(Comparator.comparing(File::getName));
                        for (File child : files) {

                            String relativePath = Utils.removeStart(child.getAbsolutePath(), SHARE_ROOT_DIR);
                            sb.append("<li><a href=\"").append("/share/"+Utils.removeStart(relativePath,"/")).append("\">").append(child.getName()).append("</li>");
                        }
                        sb.append("</ul></body></html>");
                        response.send(sb.toString());
                    } else if (file.exists()) { // 如果是文件，返回文件内容
                        response.sendFile(file);
//                        try {
////                            InputStream is = FileUtils.openInputStream(file);
////                            byte[] bytes = IOUtils.toByteArray(is);
////                            response.send(Utils.guessMimeType(filePath), bytes);
//                            response.sendFile(file);
////                            is.close();
//                        } catch (IOException e) {
//                            response.code(500);
//                            response.send("Internal Server Error: " + e.getMessage());
//                        }
                    } else {
                        response.code(404);
                        response.send("File not found.");
                    }
                    return;
                }
                response.send("jFlower (局发) is running ...");
            }
        });
//        server.route("GET","/");
        server.post("/.*", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                if(!checkRequest(request ,response))return;

                Log.i("request.getPath()", request.getPath());
                String name = Utils.urlDecode( request.getHeaders().get("name"));
                String ip = request.getHeaders().get("ip");
                switch (request.getPath()) {
                    case "/text": {

                        String res = request.getBody().get().toString();
                        Log.i("/text", res);
                        builder.setContentText(res)
                                .setContentTitle("来自："+name)
                                .setSubText(ip)
                                .clearActions()
                                .setLargeIcon(Icon.createWithResource(HttpService.this,R.drawable.logo))
                                .setWhen(System.currentTimeMillis())
                                .addAction(textAction(res));
                        if(res.startsWith("https://") || res.startsWith("http://")){
                            builder.addAction(urlAction(res));
                        }
                        notificationManager.notify(1,builder.build());
                        copy(res);
                        toast("接收到文本，已复制到剪贴板");
                        break;
                    }
                    case "/img": {
                        String res = request.getBody().get().toString();
                        Log.i("/img", res);
                        // 将 base64 字符串解码成字节数组
                        byte[] bytes = Base64.decode(res.replaceFirst("^data:.*base64,", ""), Base64.DEFAULT);
                        // 将字节数组转换为 Bitmap 对象
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        // 获取相册保存的路径
                        String savedImagePath = MediaStore.Images.Media.insertImage(
                                getContentResolver(),
                                bitmap,
                                "jFlower-" + (new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())),
                                "From " + name +"("+ ip+")\n[via jFlower]"
                        );
                        // 如果保存成功，则返回图像的 URI
                        if (savedImagePath != null) {
                            receivedImg = Uri.parse(savedImagePath);
                            builder.setContentTitle("来自："+name)
                                    .setContentText("的图片，已保存到相册")
                                    .setSubText(ip)
                                    .setLargeIcon(bitmap)
                                    .clearActions()
                                    .setWhen(System.currentTimeMillis())
                                    .addAction(imgAction(receivedImg));
                            notificationManager.notify(1,builder.build());
                            toast("接收到图片，已保存到相册");
                        } else {
                            toast("接收到图片，保存失败");
                        }
                        break;
                    }
                    case "/fileAsk": {
                        Log.i("/fileAsk", "--");
                        String fileName;
                        try {
                            fileName = URLDecoder.decode(request.getHeaders().get("file_name"),"UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            fileName = request.getHeaders().get("file_name");
                        }
                        Log.i("/fileAsk", fileName);
                        long size = Long.parseLong(request.getHeaders().get("file_size"));
                        builder.setContentText( fileName + "("+Utils.formatFileSize(size)+")")
                                .setContentTitle("来自："+name)
                                .setSubText(ip)
                                .clearActions()
                                .setLargeIcon(Icon.createWithResource(HttpService.this,R.drawable.logo))
                                .setWhen(System.currentTimeMillis())
                                .addAction(fileAction(ip,request.getHeaders().get("key") ,fileName ,name));

                        notificationManager.notify(1,builder.build());
                        toast("接收到文件请求");
                        break;
                    }
                }
                response.end();
            }
        });


        server.setErrorCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                Log.e("HTTP", ex.getMessage());
            }
        });

        server.listen(8891);

    }

    private void toast(String msg) {
        EventBus.getDefault().post(new MessageEvent(HttpService.this, MessageEvent.EVENT_TOAST, msg));
    }
    private void copy(String msg) {
        EventBus.getDefault().post(new MessageEvent(HttpService.this, MessageEvent.EVENT_COPY, msg));
    }

    private void stopServer() {
        // Stop your HTTP server here
        server.stop();
    }


}