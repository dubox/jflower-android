package com.dubox.jflower.libs;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

public class ClipBoardUtil {
    /**
     * 获取剪切板内容
     * @return
     */
    public static String paste(){
        ClipboardManager clipboard = (ClipboardManager)ActivityManager.getTopActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clipData = clipboard.getPrimaryClip();

        if (clipData != null && clipData.getItemCount() > 0) {

            CharSequence text = clipData.getItemAt(0).getText();

            String pasteString = text.toString();

            Log.d("ClipBoardUtil", "getFromClipboard text=" + pasteString);
            return pasteString;

        }
        return "";
    }

    /**
     * 清空剪切板
     */
    public static void clear(){
        ClipboardManager manager = (ClipboardManager) ActivityManager.getTopActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            try {
                manager.setPrimaryClip(manager.getPrimaryClip());
                manager.setPrimaryClip(ClipData.newPlainText("",""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
