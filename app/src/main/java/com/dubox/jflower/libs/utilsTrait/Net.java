package com.dubox.jflower.libs.utilsTrait;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;



public interface Net {

    static String localIp(Context context){
        String ip = Net.getLocalIpAddress(context);
        if(ip == null)return "";
        return ip;
    }

    static String getLocalIpAddress(Context context) {
        // 判断网络是否已连接
        if (!isNetworkConnected(context)) {
            return null;
        }

        // 获取连接的网络信息
        NetworkInfo networkInfo = getNetworkInfo(context);
        if (networkInfo == null) {
            return null;
        }

        // 如果当前网络是 WIFI 网络
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                return null;
            }

            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo == null) {
                return null;
            }

            String ipAddress = intToIp(wifiInfo.getIpAddress());
            if (ipAddress == null) {
                return null;
            }

            return ipAddress;
        }

        // 如果当前网络是移动数据网络，获取所有的 IPv4 地址，返回第一个非回环地址
//        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
//            try {
//                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
//                while (networkInterfaces.hasMoreElements()) {
//                    NetworkInterface networkInterface = networkInterfaces.nextElement();
//                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
//                    while (addresses.hasMoreElements()) {
//                        InetAddress address = addresses.nextElement();
//                        if (!address.isLoopbackAddress() && address instanceof InetAddress && address.getAddress().length == 4) {
//                            return address.getHostAddress();
//                        }
//                    }
//                }
//            } catch (SocketException e) {
//                e.printStackTrace();
//            }
//        }

        // 如果当前网络是其他类型的网络，例如以太网或蓝牙网络，获取所有的 IPv4 地址，返回第一个非回环地址
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager == null) {
                return null;
            }

            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) {
                return null;
            }

            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            if (networkCapabilities == null) {
                return null;
            }

            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) {
                try {
                    Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                    while (networkInterfaces.hasMoreElements()) {
                        NetworkInterface networkInterface = networkInterfaces.nextElement();
                        Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                        while (addresses.hasMoreElements()) {
                            InetAddress address = addresses.nextElement();
                            if (!address.isLoopbackAddress() && address instanceof InetAddress && address.getAddress().length == 4) {
                                return address.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    // 获取当前连接的网络信息
    static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return null;
        }
        return connectivityManager.getActiveNetworkInfo();
    }

    // 判断当前网络是否已连接
    static boolean isNetworkConnected(Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected();
    }

    // 将整数表示的 IP 地址转换成字符串表示的 IP 地址
    static String intToIp(int ipAddress) {
        if (ipAddress == 0) {
            return null;
        }
        return ((ipAddress & 0xff) + "." +
                ((ipAddress >> 8) & 0xff) + "." +
                ((ipAddress >> 16) & 0xff) + "." +
                ((ipAddress >> 24) & 0xff));

    }
}
