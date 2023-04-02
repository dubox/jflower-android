package com.dubox.jflower;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.dubox.jflower.libs.Utils;

public class DownloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            // 下载完成，打开已下载的文件
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                Uri fileUri = downloadManager.getUriForDownloadedFile(downloadId);
                if (fileUri != null) {
                    try {
                        Intent fileIntent = new Intent(Intent.ACTION_VIEW);
//                        fileIntent.setData(Uri.parse("content://downloads/my_downloads"));
//                        fileIntent.setData(Uri.parse("content://com.android.externalstorage.documents/document/primary:Download"));
//                        ContentResolver cr = context.getContentResolver();
                        String mimeType = Utils.guessMimeType(fileUri.toString());
                        Log.i(mimeType,fileUri.toString());
                        Toast.makeText(context, "mimeType:"+mimeType, Toast.LENGTH_LONG).show();
                        fileIntent.setDataAndType(fileUri,mimeType);
                        fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);

                            context.startActivity(Intent.createChooser(fileIntent, "Choose File Manager").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY));


                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, "无法打开该文件，请到下载目录查看", Toast.LENGTH_SHORT).show();
                        Intent fileIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                        fileIntent.setData(fileUri);
                        fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(fileIntent);
//                        openDownloadedFile( context,  downloadId);
                    }
                } else {
                    // 文件不存在，尝试重新下载
                    Toast.makeText(context, "文件不存在，请重新下载", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // 打开下载文件夹
    private void openDownloadedFile(Context context, long downloadId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            Uri uri = downloadManager.getUriForDownloadedFile(downloadId);
            Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}

