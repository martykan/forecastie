package cz.martykan.forecastie.utils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class NetworkUtils {
    private static final Pattern VPN_PATTERN = Pattern.compile("(?:tun|ppp|pptp|rndis)\\d+");

    public static boolean isNetworkAvailable(ConnectivityManager connectivityManager) {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activeNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_VPN);
            } else {
                try {
                    Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

                    while (networkInterfaces.hasMoreElements()) {
                        NetworkInterface currentNetworkInterface = networkInterfaces.nextElement();

                        if (!currentNetworkInterface.isUp()) {
                            continue;
                        }

                        String name = currentNetworkInterface.getName();

                        if (name == null) {
                            continue;
                        }

                        if (VPN_PATTERN.matcher(name).matches()) {
                            return true;
                        }
                    }
                } catch (SocketException ignore) {}
            }
        }

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
