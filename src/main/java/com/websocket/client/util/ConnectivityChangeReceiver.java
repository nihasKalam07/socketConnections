/*
 * Copyright (C) 2016 Nihas Kalam.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
