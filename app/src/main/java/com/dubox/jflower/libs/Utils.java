package com.dubox.jflower.libs;


import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.dubox.jflower.libs.utilsTrait.Net;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.content.Context.MODE_PRIVATE;

public class Utils implements Net {

   public static String getId(){
       return settingGet("localId",md5(System.currentTimeMillis()+""));
   }

   public static String getName(){
       return Settings.Secure.getString(ActivityManager.getTopActivity().getContentResolver(), "bluetooth_name");
   }


    protected static String settingGet(String name ,String def){
        SharedPreferences setting = ActivityManager.getTopActivity().getSharedPreferences("com.dubox.jflower", MODE_PRIVATE);

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
}
