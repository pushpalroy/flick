package com.pushpal.popularmoviesstage1.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class NetworkUtil {
    private Context context;

    public NetworkUtil(Context context) {
        this.context = context;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public boolean isOnline() {
        try {
            int timeoutInMilliSecs = 1500;
            Socket socket = new Socket();
            // TCP/HTTP/DNS (depending on the port, 53 = DNS, 80 = HTTP)
            // Google DNS (8.8.8.8)
            SocketAddress socketAddress = new InetSocketAddress("8.8.8.8", 53);

            socket.connect(socketAddress, timeoutInMilliSecs);
            socket.close();

            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
