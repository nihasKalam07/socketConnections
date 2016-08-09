package com.websocket.client.channel;

/**
 * Client applications should implement this interface if they want to be
 * notified when events are received on a public com.websocket.client.channel.
 *
 * <p>
 * To bind your implementation of this interface to a com.websocket.client.channel, either:
 * </p>
 * <ul>
 * <li>Call {@link com.websocket.client.QSocket#subscribe(String)} to subscribe and
 * receive an instance of {@link Channel}.</li>
 * <li>Call {@link Channel#bind(String, SubscriptionEventListener)} to bind your
 * listener to a specified event.</li>
 * </ul>
 * 
 * <p>
 * Or, call
 * {@link com.websocket.client.QSocket#subscribe(String, ChannelEventListener, String...)}
 * to subscribe to a com.websocket.client.channel and bind your listener to one or more events at the
 * same time.
 * </p>
 */
public interface ChannelEventListener extends SubscriptionEventListener {

    /**
     * <p>
     * Callback that is fired when a subscription success acknowledgement
     * message is received from QSocket after subscribing to the com.websocket.client.channel.
     * </p>
     *
     * <p>
     * For public channels this callback will be more or less immediate,
     * assuming that you are connected to QSocket at the time of subscription.
     * </p>
     *
     * @param channelName
     *            The name of the com.websocket.client.channel that was successfully subscribed.
     */
    void onSubscriptionSucceeded(String channelName);
}
