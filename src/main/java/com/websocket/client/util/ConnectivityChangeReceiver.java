package com.websocket.client.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This class is to get notify the change in network state
 */
public class ConnectivityChangeReceiver extends BroadcastReceiver {
    private ConnectivityChangeListener connectivityChangeListener;

    public interface ConnectivityChangeListener {
        void onNetworkAvailable();
    }

    public ConnectivityChangeReceiver() {
    }

    public ConnectivityChangeReceiver(ConnectivityChangeListener listener) {
        connectivityChangeListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isOnline(context) && connectivityChangeListener != null) {
            connectivityChangeListener.onNetworkAvailable();
        }
    }

    /**
     * check the network is available or not
     *
     * @param context
     * @return
     */
    private boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());

    }
}
