package com.dubox.jflower;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.core.content.FileProvider;

import static androidx.core.content.ContextCompat.startActivity;

public class MyBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_COPY = "com.dubox.jflower.ACTION_COPY";

    @Override
    public void onReceive(Context context, Intent intent) {
//
        Log.i("MyBroadcastReceiver",intent.getAction());
        if (intent.getAction().equals(ACTION_COPY)) {
            File imageFile = null;
            try {
                Log.i("imgUri",intent.getData().toString());
            // 打开输入流，读取相册图片数据
            InputStream inputStream = context.getContentResolver().openInputStream(intent.getData());
            byte[] imageBytes = IOUtils.toByteArray(inputStream);
            // 将图片保存到应用私有目录中
             imageFile = new File(context.getExternalFilesDir(null), "jFlower-"+(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))+".png");
            FileOutputStream fos = null;

                fos = new FileOutputStream(imageFile);
                fos.write(imageBytes);
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(imageFile == null) {
                Toast.makeText(context, "分享失败", Toast.LENGTH_SHORT).show();
                return;
            }
            // 获取保存后的图片的Uri
            Uri savedImageUri = FileProvider.getUriForFile(context, "com.dubox.jflower.fileprovider", imageFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, savedImageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, Intent.createChooser(shareIntent, "Share image"), PendingIntent.FLAG_UPDATE_CURRENT);
//            startActivity(context,  ,null);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
