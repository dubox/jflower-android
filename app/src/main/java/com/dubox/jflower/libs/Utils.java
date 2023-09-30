package com.dubox.jflower.libs;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.dubox.jflower.libs.utilsTrait.Net;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.MultipartFormDataBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

import static android.content.Context.MODE_PRIVATE;

public class Utils implements Net {

   public static String getId(){
       return settingGet("localId",md5(System.currentTimeMillis()+""));
   }
   public static String getId(Context context){
       return settingGet(context,"localId",md5(System.currentTimeMillis()+""));
   }

   public static String getName(){
       return Settings.Secure.getString(ActivityManager.getTopActivity().getContentResolver(), "bluetooth_name");
   }

    /**
     * 手机存储分享
     * @param context
     * @return String
     */
   public static String getStorageShare(Context context){
       return settingGet(context,"storageShare","0");
   }
   public static boolean setStorageShare(String value){
       return settingSet("storageShare",value);
   }


    protected static String settingGet(String name ,String def){

        return settingGet(ActivityManager.getTopActivity(),name, def);
    }
    protected static String settingGet(Context context, String name ,String def){
        SharedPreferences setting = context.getSharedPreferences("com.dubox.jflower", MODE_PRIVATE);

        String v = setting.getString(name, "");
        if(v == ""){
            setting.edit().putString(name, def).commit();
        }
        return setting.getString(name, "");
    }

    protected static String settingGet(String name ){
        return settingGet(name ,"");
    }


    protected static Boolean settingSet(String name ,String value ){
        SharedPreferences setting = ActivityManager.getTopActivity().getSharedPreferences("com.dubox.jflower", 0);
        return setting.edit().putString(name, value).commit();
    }

    public static String md5(String info)
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

    public static String formatFileSize(long size) {
        if (size <= 0) {
            return "0KB";
        }
        final String[] units = new String[] { "B", "KB", "MB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String urlEncode(String str){
        try {
            return URLEncoder.encode(str+"", "utf-8");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    public static String urlDecode(String str){
        try {
            return URLDecoder.decode( str+"", "utf-8");
        } catch (UnsupportedEncodingException ex) {
            return str;
        }
    }

    public static String guessMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        if (type == null) {
            type = "application/octet-stream";
        }
        return type;
    }

    public static String removeStart(String str, String remove) {
        if (str != null && remove != null && str.startsWith(remove)) {
            return str.substring(remove.length());
        }
        return str;
    }

    public static boolean isValidIpAddress(String ipAddress) {
        String regex = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,5}$";
        return ipAddress.matches(regex);
    }

    public static ColorMatrixColorFilter getGrayFilter() {
        ColorMatrix cm = new ColorMatrix();
//        cm.setSaturation(0);
        float[] matrix = {
                0.33f, 0.33f, 0.33f, 0, 0,
                0.33f, 0.33f, 0.33f, 0, 0,
                0.33f, 0.33f, 0.33f, 0, 0,
                0, 0, 0, 1, 0
        };
        cm.set(matrix);
        return new ColorMatrixColorFilter(cm);
    }

    public static PackageInfo getPackageInfo(){
        PackageManager manager = ActivityManager.getTopActivity().getPackageManager();

        try {
            return manager.getPackageInfo(ActivityManager.getTopActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void getNewVersion(String currVersion ,MyCallback cb){
        //
//        System.out.println(View.);
        MultipartFormDataBody body = new MultipartFormDataBody();
        body.addStringPart("_api_key","f8e52a2bef403133435ebdea173348b7");
        body.addStringPart("appKey","cb656120c79a59a65541ff817577a29b");
        body.addStringPart("buildVersion",currVersion);

        AsyncHttpRequest req = new AsyncHttpRequest(
                Uri.parse(
//                        "https://gitee.com/dubox/jflower-android/raw/master/version.json"
//                        "https://raw.githubusercontent.com/dubox/jflower-android/master/version.json"
//                        "https://dubox.github.io/jflower-android/version.json"
                        "https://www.pgyer.com/apiv2/app/check"
                ), "POST");
        req.setBody(body);
        AsyncHttpClient.getDefaultInstance().executeJSONObject(req, new AsyncHttpClient.JSONObjectCallback() {

            // Callback is invoked with any exceptions/errors, and the result, if available.
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse response, JSONObject result) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Looper.prepare();
                        if (e != null) {
                            System.out.println(e.toString());
                            //e.printStackTrace();
                            Toast.makeText(ActivityManager.getTopActivity(),"网络异常，请稍后重试",Toast.LENGTH_LONG).show();
                            return;
                        }
                        System.out.println("I got a JSONObject: " + result);
                        try {
                            if((int)result.get("code") != 0){
                                Toast.makeText(ActivityManager.getTopActivity(),(String)result.get("message"),Toast.LENGTH_LONG).show();
                                return;
                            }
                            JSONObject data = (JSONObject) result.get("data");
                            cb.onNewVersion(data);
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                        Looper.loop();
                    }
                }).start();

            }
        });
    }
}
