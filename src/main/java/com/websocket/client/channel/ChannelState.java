package com.websocket.client.channel;

/**
 * Used to identify the state of the com.websocket.client.channel e.g. subscribed or unsubscribed.
 */
public enum ChannelState {
    INITIAL, SUBSCRIBE_SENT, SUBSCRIBED, UNSUBSCRIBED, FAILED
}
