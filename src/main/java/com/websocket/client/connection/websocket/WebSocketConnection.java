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
package com.websocket.client.connection.websocket;

import android.util.Log;

import com.google.gson.Gson;
import com.websocket.client.connection.ConnectionEventListener;
import com.websocket.client.connection.ConnectionState;
import com.websocket.client.connection.ConnectionStateChange;
import com.websocket.client.connection.impl.InternalConnection;
import com.websocket.client.util.Constants;
import com.websocket.client.util.Factory;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;


public class WebSocketConnection implements InternalConnection, WebSocketListener {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConnection.class);
    private static final Gson GSON = new Gson();
    private static final String CONNECTION_ESTABLISHED_EVENT = "101";
    private static final String CONNECTION_ERROR_EVENT = "102";

    private static final String INTERNAL_EVENT_PREFIX = "qsocket:";
    static final String PING_EVENT_SERIALIZED = "{\"event\": \"qsocket:ping\"}";

    private final Factory factory;
    private final ActivityTimer activityTimer;
    private final Map<ConnectionState, Set<ConnectionEventListener>> eventListeners = new ConcurrentHashMap<ConnectionState, Set<ConnectionEventListener>>();
    private final URI webSocketUri;
    private final Proxy proxy;

    private volatile ConnectionState state = ConnectionState.DISCONNECTED;
    private WebSocketClient underlyingConnection;
    private HashMap<String, String> header;

    public WebSocketConnection(
            final String url,
            final long activityTimeout,
            final long pongTimeout,
            final Proxy proxy,
            final Factory factory,
            final HashMap<String, String> header) throws URISyntaxException {
        webSocketUri = new URI(url);
        activityTimer = new ActivityTimer(activityTimeout, pongTimeout);
        this.proxy = proxy;
        this.factory = factory;
        this.header = header;

        for (final ConnectionState state : ConnectionState.values()) {
            eventListeners.put(state, Collections.newSetFromMap(new ConcurrentHashMap<ConnectionEventListener, Boolean>()));
        }
    }

    /* Connection implementation */

    @Override
    public void connect() {
        factory.queueOnEventThread(new Runnable() {

            @Override
            public void run() {
                if (state == ConnectionState.DISCONNECTED) {
                    try {
                        underlyingConnection = factory
                                .newWebSocketClientWrapper(webSocketUri, proxy, WebSocketConnection.this, header);
                        updateState(ConnectionState.CONNECTING);
                        underlyingConnection.connect();
                    }
                    catch (final SSLException e) {
                        sendErrorToAllListeners("Error connecting over SSL", null, e);
                    }
                }
            }
        });
    }

    @Override
    public void disconnect() {
        factory.queueOnEventThread(new Runnable() {
            @Override
            public void run() {
                if (state == ConnectionState.CONNECTED) {
                    updateState(ConnectionState.DISCONNECTING);
                    underlyingConnection.close();
                }
            }
        });
    }

    @Override
    public void bind(final ConnectionState state, final ConnectionEventListener eventListener) {
        eventListeners.get(state).add(eventListener);
    }

    @Override
    public boolean unbind(final ConnectionState state, final ConnectionEventListener eventListener) {
        return eventListeners.get(state).remove(eventListener);
    }

    @Override
    public ConnectionState getState() {
        return state;
    }

    /* InternalConnection implementation detail */

    @Override
    public void sendMessage(final String message) {
        factory.queueOnEventThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (state == ConnectionState.CONNECTED) {
                        underlyingConnection.send(message);
                    }
                    else {
                        sendErrorToAllListeners("Cannot send a message while in " + state + " state", null, null);
                    }
                }
                catch (final Exception e) {
                    sendErrorToAllListeners("An exception occurred while sending message [" + message + "]", null, e);
                }
            }
        });
    }

    /* implementation detail */

    private void updateState(final ConnectionState newState) {
        log.debug("State transition requested, current [" + state + "], new [" + newState + "]");

        final ConnectionStateChange change = new ConnectionStateChange(state, newState);
        state = newState;

        final Set<ConnectionEventListener> interestedListeners = new HashSet<ConnectionEventListener>();
        interestedListeners.addAll(eventListeners.get(ConnectionState.ALL));
        interestedListeners.addAll(eventListeners.get(newState));

        for (final ConnectionEventListener listener : interestedListeners) {
            factory.queueOnEventThread(new Runnable() {
                @Override
                public void run() {
                    listener.onConnectionStateChange(change);
                }
            });
        }
    }

//    private void handleEvent(final String event, final String wholeMessage) {
//        if (event.startsWith(INTERNAL_EVENT_PREFIX)) {
//            handleInternalEvent(event, wholeMessage);
//        }
//        else {
//            factory.getChannelManager().onMessage(event, wholeMessage);
//        }
//    }

    private void handleEvent(final String event, final String wholeMessage) {
        switch (event) {
            case CONNECTION_ESTABLISHED_EVENT:
                handleConnectionMessage(wholeMessage);
                break;
            case CONNECTION_ERROR_EVENT:
                handleError(wholeMessage);
                break;
            default:
                factory.getChannelManager().onMessage(event, wholeMessage);
        }
    }

//    private void handleInternalEvent(final String event, final String wholeMessage) {
//        if (event.equals("qsocket:connection_established")) {
//            handleConnectionMessage(wholeMessage);
//        }
//        else if (event.equals("qsocket:error")) {
//            handleError(wholeMessage);
//        }
//    }

    @SuppressWarnings("rawtypes")
    private void handleConnectionMessage(final String message) {
        final Map jsonObject = GSON.fromJson(message, Map.class);
        final String dataString = (String)jsonObject.get(Constants.MESSAGE);
        updateState(ConnectionState.CONNECTED);
    }

    @SuppressWarnings("rawtypes")
    private void handleError(final String wholeMessage) {
        final Map json = GSON.fromJson(wholeMessage, Map.class);

        final String message = (String)json.get(Constants.MESSAGE);
        final String code = (String)json.get(Constants.EVENT_TYPE);

        sendErrorToAllListeners(message, code, null);
    }

    private void sendErrorToAllListeners(final String message, final String code, final Exception e) {
        final Set<ConnectionEventListener> allListeners = new HashSet<ConnectionEventListener>();
        for (final Set<ConnectionEventListener> listenersForState : eventListeners.values()) {
            allListeners.addAll(listenersForState);
        }

        for (final ConnectionEventListener listener : allListeners) {
            factory.queueOnEventThread(new Runnable() {
                @Override
                public void run() {
                    listener.onError(message, code, e);
                }
            });
        }
    }

    /* WebSocketListener implementation */

    @Override
    public void onOpen(final ServerHandshake handshakedata) {
        Log.i("WebsocketConnection", "Opened");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(final String message) {
        activityTimer.activity();

        factory.queueOnEventThread(new Runnable() {
            @Override
            public void run() {
                final Map<String, String> map = GSON.fromJson(message, Map.class);
                final String event = map.get(Constants.EVENT_TYPE);
                handleEvent(event, message);
            }
        });
    }

    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        activityTimer.cancelTimeouts();

        factory.queueOnEventThread(new Runnable() {
            @Override
            public void run() {
                if (state != ConnectionState.DISCONNECTED) {
                    updateState(ConnectionState.DISCONNECTED);
                }
                else {
                    log.error("Received close from underlying socket when already disconnected. " + "Close code ["
                            + code + "], Reason [" + reason + "], Remote [" + remote + "]");
                }
                factory.shutdownThreads();
            }
        });
    }

    @Override
    public void onError(final Exception ex) {
        factory.queueOnEventThread(new Runnable() {
            @Override
            public void run() {
                // Do not change com.websocket.client.connection state as Java_WebSocket will also
                // call onClose.
                // See:
                // https://github.com/leggetter/pusher-java-client/issues/8#issuecomment-16128590
                // updateState(ConnectionState.DISCONNECTED);
                sendErrorToAllListeners("An exception was thrown by the websocket", null, ex);
            }
        });
    }

    private class ActivityTimer {
        private final long activityTimeout;
        private final long pongTimeout;

        private Future<?> pingTimer;
        private Future<?> pongTimer;

        public ActivityTimer(final long activityTimeout, final long pongTimeout) {
            this.activityTimeout = activityTimeout;
            this.pongTimeout = pongTimeout;
        }

        /**
         * On any activity from the server - Cancel pong timeout - Cancel
         * currently ping timeout and re-schedule
         */
        public synchronized void activity() {
            if (pongTimer != null) {
                pongTimer.cancel(true);
            }

            if (pingTimer != null) {
                pingTimer.cancel(false);
            }
            pingTimer = factory.getTimers().schedule(new Runnable() {
                @Override
                public void run() {
                    log.debug("Sending ping");
                    sendMessage(PING_EVENT_SERIALIZED);
                    schedulePongCheck();
                }
            }, activityTimeout, TimeUnit.MILLISECONDS);
        }

        /**
         * Cancel any pending timeouts, for example because we are disconnected.
         */
        public synchronized void cancelTimeouts() {
            if (pingTimer != null) {
                pingTimer.cancel(false);
            }
            if (pongTimer != null) {
                pongTimer.cancel(false);
            }
        }

        /**
         * Called when a ping is sent to await the response - Cancel any
         * existing timeout - Schedule new one
         */
        private synchronized void schedulePongCheck() {
            if (pongTimer != null) {
                pongTimer.cancel(false);
            }

            pongTimer = factory.getTimers().schedule(new Runnable() {
                @Override
                public void run() {
                    log.debug("Timed out awaiting pong from server - disconnecting");
                    disconnect();
                }
            }, pongTimeout, TimeUnit.MILLISECONDS);
        }
    }
}
