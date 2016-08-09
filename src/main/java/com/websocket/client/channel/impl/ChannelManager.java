package com.websocket.client.channel.impl;

import android.util.Log;

import com.google.gson.Gson;
import com.websocket.client.channel.Channel;
import com.websocket.client.channel.ChannelEventListener;
import com.websocket.client.channel.ChannelState;
import com.websocket.client.channel.ChannelUnsubscriptionEventListener;
import com.websocket.client.connection.ConnectionEventListener;
import com.websocket.client.connection.ConnectionState;
import com.websocket.client.connection.ConnectionStateChange;
import com.websocket.client.connection.impl.InternalConnection;
import com.websocket.client.util.Constants;
import com.websocket.client.util.Factory;

import java.util.HashMap;
import java.util.Map;

public class ChannelManager implements ConnectionEventListener {

    private static final Gson GSON = new Gson();
    private final Map<String, InternalChannel> channelNameToChannelMap = new HashMap<String, InternalChannel>();
    private final Factory factory;
    private InternalConnection connection;

    public ChannelManager(final Factory factory) {
        this.factory = factory;
    }

    public Channel getChannel(String channelName) {
        return (Channel) findChannelInChannelMap(channelName);
    }

    private InternalChannel findChannelInChannelMap(String channelName) {
        return channelNameToChannelMap.get(channelName);
    }

    public void setConnection(final InternalConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Cannot construct ChannelManager with a null com.websocket.client.connection");
        }

        if (this.connection != null) {
            this.connection.unbind(ConnectionState.CONNECTED, this);
        }

        this.connection = connection;
        connection.bind(ConnectionState.CONNECTED, this);
    }

    public void subscribeTo(final InternalChannel channel, final ChannelEventListener listener,
                            final String... eventNames) {
        try {
            validateArgumentsAndBindEvents(channel, listener, eventNames);
            channelNameToChannelMap.put(channel.getName(), channel);
            sendOrQueueSubscribeMessage(channel);
        } catch (IllegalArgumentException e) {
            Log.v("message", e.getMessage());
        }

    }

    public void unsubscribeFrom(final String channelName, final ChannelUnsubscriptionEventListener channelUnsubscriptionEventListeneristener) {

        if (channelName == null) {
            throw new IllegalArgumentException("Cannot unsubscribe from null com.websocket.client.channel");
        }

        final InternalChannel channel = channelNameToChannelMap.remove(channelName);
        if (channel == null) {
            return;
        }
        channel.setUnsubscribeEventListener(channelUnsubscriptionEventListeneristener);
        if (connection.getState() == ConnectionState.CONNECTED) {
            sendUnsubscribeMessage(channel);
        }
    }

    @SuppressWarnings("unchecked")
    public void onMessage(final String event, final String wholeMessage) {

        final Map<Object, Object> json = GSON.fromJson(wholeMessage, Map.class);
        final Object channelNameObject = json.get(Constants.CHANNEL);

        if (channelNameObject != null) {
            final String channelName = (String) channelNameObject;
            final InternalChannel channel = channelNameToChannelMap.get(channelName);

            if (channel != null) {
                channel.onMessage(event, wholeMessage);
            }
        }
    }

    /* ConnectionEventListener implementation */

    @Override
    public void onConnectionStateChange(final ConnectionStateChange change) {

        if (change.getCurrentState() == ConnectionState.CONNECTED) {

            for (final InternalChannel channel : channelNameToChannelMap.values()) {
                sendOrQueueSubscribeMessage(channel);
            }
        }
    }

    @Override
    public void onError(final String message, final String code, final Exception e) {
        // ignore or log
    }

    /* implementation detail */

    private void sendOrQueueSubscribeMessage(final InternalChannel channel) {

        factory.queueOnEventThread(new Runnable() {

            @Override
            public void run() {

                if (connection.getState() == ConnectionState.CONNECTED) {
                    final String message = channel.toSubscribeMessage();
                    connection.sendMessage(message);
                    channel.updateState(ChannelState.SUBSCRIBE_SENT);
                }
            }
        });
    }

    private void sendUnsubscribeMessage(final InternalChannel channel) {
        factory.queueOnEventThread(new Runnable() {
            @Override
            public void run() {
                connection.sendMessage(channel.toUnsubscribeMessage());
                channel.updateState(ChannelState.UNSUBSCRIBED);
            }
        });
    }

    private void clearDownSubscription(final InternalChannel channel, final Exception e) {

        channelNameToChannelMap.remove(channel.getName());
        channel.updateState(ChannelState.FAILED);
    }

    private void validateArgumentsAndBindEvents(final InternalChannel channel, final ChannelEventListener listener,
                                                final String... eventNames) {

        if (channel == null) {
            throw new IllegalArgumentException("Cannot subscribe to a null com.websocket.client.channel");
        }

        if (channelNameToChannelMap.containsKey(channel.getName())) {
            throw new IllegalArgumentException("Already subscribed to a com.websocket.client.channel with name " + channel.getName());
        }

        for (final String eventName : eventNames) {
            channel.bind(eventName, listener);
        }
//        channel.bind(channel.getName(), listener);

        channel.setEventListener(listener);
    }
}
