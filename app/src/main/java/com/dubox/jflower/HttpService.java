package com.dubox.jflower;

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
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.dubox.jflower.libs.ClipBoardUtil;
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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.ThemeUtils;
import androidx.core.app.NotificationCompat;

public class HttpService extends Service {
    public HttpService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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

        Notification notification =
                new NotificationCompat.Builder(this, createNotificationChannel("h1","http-service"))
                        .setContentTitle("notification_title")
                        .setContentText("notification_message")
                        .setSmallIcon(R.drawable.logo)
                        .setContentIntent(pendingIntent)
                        .setTicker("ticker_text")
                        .addAction(getAction())
                        .build();
        Log.i("notification","ok");
        // Notification ID cannot be 0.
        startForeground(1, notification);
        startServer();
        // If we get killed, after returning from here, restart
//        return super.onStartCommand(intent,flags,startId);
        return START_STICKY;
    }

    private NotificationCompat.Action getAction(){
        // 创建一个 Intent ，用于点击操作按钮时触发
        Intent intent = new Intent(this, MyBroadcastReceiver.class);
        intent.setAction(MyBroadcastReceiver.ACTION_COPY);

// 创建一个 PendingIntent ，用于在通知中添加操作按钮
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
// 获取主题中的图标属性
        TypedArray styledAttributes = this.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionModeCopyDrawable });

// 从 TypedArray 中获取图标资源 ID
        int iconResId = styledAttributes.getResourceId(0, 0);
// 创建一个操作按钮，并将其添加到通知中
        return new NotificationCompat.Action.Builder(
                iconResId,  // 图标
                "Copy",  // 操作按钮标题
                pendingIntent  // 点击操作按钮时触发的 PendingIntent
        ).build();
    }

    /**
     * 创建通知通道
     * @param channelId
     * @param channelName
     * @return
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName){
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        Log.i("createNotificationChannel","ok");
        return channelId;
    }

    @Override
    public void onDestroy() {
        Log.e("HttpService", "onDestroy");
        stopServer();
        // 移除通知
        stopForeground(true);
        super.onDestroy();
    }

    private void startServer() {
        // Create and start your HTTP server here
        AsyncHttpServer server = new AsyncHttpServer();

        server.get("/.*", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                if(request.getPath().equals("/detect")){
                    response.getHeaders().set("id","test111");
                    response.getHeaders().set("name","test");
                }
                response.send("jFlower (局发) is running ...");
            }
        });
//        server.route("GET","/");
        server.post("/.*", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {


                switch (request.getPath()){
                    case "/text":{

                        Log.i("ssss",request.getBody().get().toString());
                        // 获取剪贴板管理器
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                        ClipData clip = ClipData.newPlainText("text", request.getBody().get().toString());
                        clipboard.setPrimaryClip(clip);
//                        Toast.makeText(HttpService.this, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                response.send("jFlower (局发) is running ...");
            }
        });

// listen on port 5000
        server.listen(8891);
    }

    private void stopServer() {
        // Stop your HTTP server here
    }


/**
    private void startServerThread() {
        serverThread = new Thread(new HttpServerRunnable());
        serverThread.start();
    }

    private void stopServerThread() {
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    private class HttpServerRunnable implements Runnable {
        @Override
        public void run() {
            try {
                // Create and start your HTTP server here
                httpServer = new HttpServer();
                httpServer.start();
            } catch (IOException e) {
                Log.e("HttpServerService", "Failed to start HTTP server", e);
            }
        }
    }
*/

/*

//启动服务
if (!ForegroundService.serviceIsLive) {
    // Android 8.0使用startForegroundService在前台启动新服务
    mForegroundService = new Intent(this, ForegroundService.class);
    mForegroundService.putExtra("Foreground", "This is a foreground service.");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(mForegroundService);
    } else {
        startService(mForegroundService);
    }
} else {
    Toast.makeText(this, "前台服务正在运行中...", Toast.LENGTH_SHORT).show();
}


 */

}