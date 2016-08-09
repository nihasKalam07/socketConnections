package com.websocket.client.channel.impl;

import com.websocket.client.channel.Channel;
import com.websocket.client.channel.ChannelEventListener;
import com.websocket.client.channel.ChannelState;
import com.websocket.client.channel.ChannelUnsubscriptionEventListener;

public interface InternalChannel extends Channel, Comparable<InternalChannel> {

    String toSubscribeMessage();

    String toUnsubscribeMessage();

    void onMessage(String event, String message);

    void updateState(ChannelState state);

    void setEventListener(ChannelEventListener listener);

    void setUnsubscribeEventListener(ChannelUnsubscriptionEventListener listener);

    ChannelEventListener getEventListener();
}
