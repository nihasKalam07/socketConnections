package com.websocket.client;


import com.websocket.client.channel.Channel;
import com.websocket.client.channel.ChannelEventListener;
import com.websocket.client.connection.Connection;
import com.websocket.client.connection.ConnectionEventListener;
import com.websocket.client.connection.ConnectionState;

public interface Client {
    Connection getConnection();
    void connect();
    void connect(final ConnectionEventListener eventListener, ConnectionState... connectionStates);
    void disconnect();
    Channel subscribe(final String channelName);
    Channel subscribe(final String channelName, final ChannelEventListener listener, final String... eventNames);
    void unsubscribe(final String channelName);
    Channel getChannel(String channelName);
}
