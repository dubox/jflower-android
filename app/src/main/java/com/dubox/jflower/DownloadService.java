package com.dubox.jflower;

import android.Manifest;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.dubox.jflower.libs.Utils;
import com.dubox.jflower.libs.utilsTrait.Net;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import permison.PermissonUtil;
import permison.listener.PermissionListener;

import static androidx.core.app.ActivityCompat.requestPermissions;

public class DownloadService extends Service {
    private static final int NOTIFICATION_ID = 2;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;

    private static final int PERMISSION_REQUEST_CODE = 100;

    private DownloadReceiver downloadReceiver;

    long downloadingId;

    private ContentObserver downloadObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
//            Intent fileIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
//        fileIntent.setData(downloadManager.getUriForDownloadedFile(downloadId));
//        fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(fileIntent);
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadingId);

            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(index);
                switch (status) {
                    case DownloadManager.STATUS_RUNNING:
//                    index = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
//                    int totalSize = cursor.getInt(index);
//                    index = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
//                    int downloadSize = cursor.getInt(index);
//                    int progress = downloadSize * 100 / totalSize;
//                    // 更新下载进度
//                    notificationBuilder.setProgress(100, progress, false);
//                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        // 下载完成
                        Log.i("progress", "onSuccess");
                    openFile(downloadManager,downloadingId);
//                        try {
//                            downloadManager.openDownloadedFile(downloadingId);
//
//                        } catch (FileNotFoundException e) {
//                            throw new RuntimeException(e);
//                        }

//                            .setContentTitle("接收完毕")
//                            .setContentText("点击查看")
//                            .setAutoCancel(true)
//                            .setContentIntent(getOpenDirIntent(response));
////                                .setContentIntent(getOpenFileIntent(response));
////                                .addAction(imgAction(receivedImg));
//                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                        break;
                    case DownloadManager.STATUS_FAILED:
                        // 下载失败
                        break;
                }
            }
            cursor.close();
        }
    };

    private void openFile(DownloadManager downloadManager, long downloadId) {
        Uri fileUri = downloadManager.getUriForDownloadedFile(downloadId);
        if (fileUri != null) {
            try {
//                Intent fileIntent = new Intent(Intent.ACTION_VIEW);
//                        fileIntent.setData(Uri.parse("content://downloads/my_downloads"));
//                        fileIntent.setData(Uri.parse("content://com.android.externalstorage.documents/document/primary:Download"));
//                        ContentResolver cr = context.getContentResolver();
//                String mimeType = Utils.guessMimeType(fileUri.toString());
                String mimeType = downloadManager.getMimeTypeForDownloadedFile(downloadId);
//                Log.i(mimeType, fileUri.toString());
//                Toast.makeText(this, "mimeType:" + mimeType, Toast.LENGTH_LONG).show();
//                fileIntent.setDataAndType(fileUri, mimeType);
                Intent fileIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
//                fileIntent.setDataAndType(fileUri, mimeType);
//                fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);

//                this.startActivity(Intent.createChooser(fileIntent, "Choose File Manager").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY));
                startActivity(fileIntent);

            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "无法打开该文件，请到下载目录查看", Toast.LENGTH_SHORT).show();
                Intent fileIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                fileIntent.setData(fileUri);
                fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(fileIntent);
//                        openDownloadedFile( context,  downloadId);
            }
        } else {
            // 文件不存在，尝试重新下载
            Toast.makeText(this, "文件不存在，请重新下载", Toast.LENGTH_SHORT).show();
        }
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
                    channelName, NotificationManager.IMPORTANCE_LOW);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            service.createNotificationChannel(chan);
            Log.i("createNotificationChannel", "ok");
        }
        return channelId;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra("fileUrl");
        String fileName = intent.getStringExtra("fileName");
        String key = intent.getStringExtra("key");
        String dir = newFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),fileName).getAbsolutePath();

//        File dir = getFilesDir();//.getAbsolutePath();
//        File dir = newFile(getExternalFilesDir(null), fileName);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this, createNotificationChannel("d1", "download"))
                .setContentTitle(fileName)
                .setContentText("正在下载...")
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        startForeground(NOTIFICATION_ID, notificationBuilder.build());

//        downloadReceiver = new DownloadReceiver();
//        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
//        registerReceiver(downloadReceiver, filter);

//        getContentResolver().registerContentObserver(
//                Uri.parse("content://downloads/my_downloads"),
//                true,
//                downloadObserver);
        // Start the download
//        execDownload(url, fileName, key);
        execDownload(url,fileName,key ,dir);

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.e("DownloadService", "onDestroy");
        if (downloadReceiver != null) {
            unregisterReceiver(downloadReceiver);
            downloadReceiver = null;
        }
        getContentResolver().unregisterContentObserver(downloadObserver);

        super.onDestroy();
    }



    private void execDownload(String url, String fileName, String fileKey) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle(fileName);
//        request.setMimeType(mimeType);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        request.addRequestHeader("file_name", Utils.urlEncode(fileName))
                .addRequestHeader("key", fileKey)
                .addRequestHeader("range", "bytes=0-")
                .addRequestHeader("cmd", "getFile")
                .addRequestHeader("ip", Net.localIp(this))
                .addRequestHeader("id", Utils.getId(this));
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        downloadingId = downloadManager.enqueue(request);
        Log.i("downloadId1", downloadingId + "");


    }

    private void execDownload(String url, String fileName, String fileKey, String dir) {
        AsyncHttpClient.getDefaultInstance().executeFile(
                new AsyncHttpRequest(Uri.parse(url), "GET")
                        .setHeader("file_name", Utils.urlEncode(fileName))
                        .setHeader("key", fileKey)
                        .setHeader("range", "bytes=0-")
                        .setHeader("cmd", "getFile")
                        .setHeader("ip", Net.localIp(this))
                        .setHeader("id", Utils.getId(this)),
                dir,
                new AsyncHttpClient.FileCallback() {
                    @Override
                    public void onCompleted(Exception e, AsyncHttpResponse source, File result) {
                        if (e != null) {
                            e.printStackTrace();
                            onFailure();
                        } else {
                            onSuccess(result);
                        }
                    }

                    @Override
                    public void onProgress(AsyncHttpResponse response, long downloaded, long total) {
                        int progress = (int) (downloaded * 100 / total);
                        Log.i("progress", progress + "");
                        notificationBuilder.setProgress(100, progress, false);
                        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                    }


                    public void onSuccess(File response) {
                        Log.i("progress", "onSuccess");
//                        response = moveToDownload(response);
                        // Download complete, show notification
                        notificationBuilder.setProgress(0, 0, false)
                                .setContentTitle("点击查看")
                                .setContentText("接收完毕（保存在下载目录）")
                                .setAutoCancel(true)
                                .setContentIntent(getOpenFileIntent(response));
//                                .setContentIntent(getOpenFileIntent(response));
//                                .addAction(imgAction(receivedImg));
                        notificationManager.notify(1, notificationBuilder.build());

//                        notificationBuilder.setContentText( fileName + "("+Utils.formatFileSize(size)+")")
//                                .setContentTitle("来自："+name)
//                                .setSubText(ip)
//                                .addAction(fileAction(response));
//                        notificationManager.notify(1, notificationBuilder.build());

                        stopSelf();
                    }


                    public void onFailure() {
                        Log.i("progress", "onFailure");
                        // Download failed, show notification
                        notificationBuilder.setProgress(0, 0, false)
                                .setContentText("接收失败")
                                .setAutoCancel(true);
                        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                        stopSelf();
                    }

                });

    }

    private NotificationCompat.Action fileAction(File file) {
        PendingIntent pendingIntent = getOpenFileIntent(file);
        return new NotificationCompat.Action.Builder(
                R.drawable.ic_menu_camera,  // 图标
                "点击查看",  // 操作按钮标题
                pendingIntent  // 点击操作按钮时触发的 PendingIntent
        ).build();
    }

    private PendingIntent getOpenFileIntent(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", file);
//            uri = Uri.fromFile(file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        intent.setDataAndType(uri, getMimeType(file.getAbsolutePath()));
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getOpenDirIntent(File file) {
//
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", file);
//        Uri fileUri = Uri.fromFile(dir);
//        intent.setDataAndType(fileUri, "vnd.android.document/directory");
        intent.setDataAndType(fileUri, "*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    private PendingIntent getOpenDirIntent3(File file) {
//        Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download");
        Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download");
//        Uri uri = Uri.parse("content://downloads/all_downloads");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getOpenDirIntent4(File file) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)), "file/*");
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private String getMimeType(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    File newFile(File path, String name) {
        path = name.equals("") ? path : new File(path, name);
        int i = 1;
        while (path.exists()) {
            path = new File(path.getParent(), "("+i+")" + path.getName());
            i++;
        }
        return path;
    }

    File moveToDownload(File internalFile) {
//        File internalFile = new File(getFilesDir(), "example.txt");
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File destFile = newFile(downloadDir, internalFile.getName());

        try {
            FileInputStream fis = new FileInputStream(internalFile);
            FileOutputStream fos = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fis.close();
            fos.close();

            MediaScannerConnection.scanFile(this, new String[]{destFile.getPath()}, null, null);
            return destFile;
        } catch (IOException e) {
            e.printStackTrace();
            return internalFile;
        }
    }
}
