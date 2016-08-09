package com.websocket.client.channel;

/**
 * Client applications should implement this interface if they want to be
 * notified when un-subscribe a com.websocket.client.channel.
 * <p>
 * Or, call
 * {@link com.websocket.client.QSocket#unsubscribe(String)} (String, ChannelUnsubscriptionEventListener, String...)}
 * to subscribe to a com.websocket.client.channel and bind your listener to one or more events at the
 * same time.
 * </p>
 */
public interface ChannelUnsubscriptionEventListener {

    /**
     * <p>
     * Callback that is fired when a un-subscription success acknowledgement
     * message is received from QSocket after subscribing to the com.websocket.client.channel.
     * </p>
     * @param channelName
     *            The name of the com.websocket.client.channel that was successfully subscribed.
     */
    void onUnsubscribed(String channelName);
}
