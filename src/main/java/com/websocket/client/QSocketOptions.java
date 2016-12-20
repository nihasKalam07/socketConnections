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
package com.websocket.client;

import java.net.Proxy;
import java.util.HashMap;

/**
 * Configuration for a {@link com.websocket.client.QSocket} instance.
 */
public class QSocketOptions {

    private static final String URI_SUFFIX = "uri_suffix";
    private static final String WS_SCHEME = "ws";
    private static final String WSS_SCHEME = "wss";

    private static final int WS_PORT = 4444;//80;
    private static final int WSS_PORT = 4444;//443;
    private static final String QSOCKET_DOMAIN = "qsocket.com";

    private static final long DEFAULT_ACTIVITY_TIMEOUT = 120000;
    private static final long DEFAULT_PONG_TIMEOUT = 30000;
    private static final String AUTHORIZATION = "Authorization";

    private String host = "10.3.1.181";
    private int wsPort = WS_PORT;
    private int wssPort = WSS_PORT;
    private boolean encrypted = false;//true;
    private long activityTimeout = DEFAULT_ACTIVITY_TIMEOUT;
    private long pongTimeout = DEFAULT_PONG_TIMEOUT;
    private Proxy proxy = Proxy.NO_PROXY;
    private String authorizationToken = "1234567890";

    /**
     * Gets whether an encrypted (SSL) connection should be used when connecting
     * to QSocket.
     *
     * @return true if an encrypted connection should be used; otherwise false.
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * Sets whether an encrypted (SSL) connection should be used when connecting to
     * QSocket.
     *
     * @param encrypted Whether to use an SSL connection
     * @return this, for chaining
     */
    public QSocketOptions setEncrypted(final boolean encrypted) {
        this.encrypted = encrypted;
        return this;
    }

    /**
     * The host to which connections will be made.
     *
     * Note that if you wish to connect to a standard QSocket cluster, the
     * convenience method setCluster will set the host and ports correctly from
     * a single argument.
     *
     * @param host The host
     * @return this, for chaining
     */
    public QSocketOptions setHost(final String host) {
        this.host = host;
        return this;
    }

    /**
     * The port to which unencrypted connections will be made.
     *
     * Note that if you wish to connect to a standard QSocket cluster, the
     * convenience method setCluster will set the host and ports correctly from
     * a single argument.
     *
     * @param wsPort port number
     * @return this, for chaining
     */
    public QSocketOptions setWsPort(final int wsPort) {
        this.wsPort = wsPort;
        return this;
    }

    /**
     * The port to which encrypted connections will be made.
     *
     * Note that if you wish to connect to a standard QSocket cluster, the
     * convenience method setCluster will set the host and ports correctly from
     * a single argument.
     *
     * @param wssPort port number
     * @return this, for chaining
     */
    public QSocketOptions setWssPort(final int wssPort) {
        this.wssPort = wssPort;
        return this;
    }

    public QSocketOptions setCluster(final String cluster) {
        host = "ws-" + cluster + "." + QSOCKET_DOMAIN;
        wsPort = WS_PORT;
        wssPort = WSS_PORT;
        return this;
    }

    /**
     * The number of milliseconds of inactivity at which a "ping" will be
     * triggered to check the connection.
     *
     * The default value is 120,000 (2 minutes). On some connections, where
     * intermediate hops between the application and QSocket are aggressively
     * culling connections they consider to be idle, a lower value may help
     * preserve the connection.
     *
     * @param activityTimeout
     *            time to consider connection idle, in milliseconds
     * @return this, for chaining
     */
    public QSocketOptions setActivityTimeout(final long activityTimeout) {
        if (activityTimeout < 1000) {
            throw new IllegalArgumentException(
                    "Activity timeout must be at least 1,000ms (and is recommended to be much higher)");
        }

        this.activityTimeout = activityTimeout;
        return this;
    }

    public long getActivityTimeout() {
        return activityTimeout;
    }

    /**
     * The number of milliseconds after a "ping" is sent that the client will
     * wait to receive a "pong" response from the server before considering the
     * connection broken and triggering a transition to the disconnected state.
     *
     * The default value is 30,000.
     *
     * @param pongTimeout
     *            time to wait for pong response, in milliseconds
     * @return this, for chaining
     */
    public QSocketOptions setPongTimeout(final long pongTimeout) {
        if (pongTimeout < 1000) {
            throw new IllegalArgumentException(
                    "Pong timeout must be at least 1,000ms (and is recommended to be much higher)");
        }

        this.pongTimeout = pongTimeout;
        return this;
    }

    public long getPongTimeout() {
        return pongTimeout;
    }

    /**
     * Construct the URL for the WebSocket connection based on the options
     * previous set on this object and the provided API key
     *
     * @param apiKey The API key
     * @return the WebSocket URL
     */
    public String buildUrl() {
//        return String.format("%s://%s:%s/app/%s%s", encrypted ? WSS_SCHEME : WS_SCHEME, host, encrypted ? wssPort
//                : wsPort, apiKey, URI_SUFFIX);
        return String.format("%s://%s:%s", encrypted ? WSS_SCHEME : WS_SCHEME, host, encrypted ? wssPort
                : wsPort);
//        return "ws://10.3.1.181:4444";
    }

    /**
     *
     * The default value is Proxy.NO_PROXY.
     *
     * @param proxy
     *            Specify a proxy, e.g. <code>options.setProxy( new Proxy( Proxy.Type.HTTP, new InetSocketAddress( "proxyaddress", 80 ) ) )</code>;
     * @return this, for chaining
     */
    public QSocketOptions setProxy(Proxy proxy){
        if (proxy == null) {
          throw new IllegalArgumentException("proxy must not be null (instead use Proxy.NO_PROXY)");
        }
        this.proxy = proxy;
        return this;
    }

    /**
     * @return The proxy to be used when opening a websocket connection to QSocket.
     */
    public Proxy getProxy() {
        return this.proxy;
    }

    public QSocketOptions setAuthorizationToken(String authorizationToken) {
        if (authorizationToken == null ) {
            throw new IllegalArgumentException(
                    "authorizationToken must not be null");
        }
        this.authorizationToken = authorizationToken;
        return this;
    }

    public HashMap<String, String> getUrlHeader() {
        HashMap<String, String> header = new HashMap<String, String>();
        header.put(AUTHORIZATION, authorizationToken);
        return header;
    }
}
