package com.websocket.client;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.websocket.client.channel.Channel;
import com.websocket.client.channel.ChannelEventListener;
import com.websocket.client.channel.ChannelUnsubscriptionEventListener;
import com.websocket.client.channel.impl.ChannelManager;
import com.websocket.client.channel.impl.InternalChannel;
import com.websocket.client.connection.Connection;
import com.websocket.client.connection.ConnectionEventListener;
import com.websocket.client.connection.ConnectionState;
import com.websocket.client.connection.impl.InternalConnection;
import com.websocket.client.util.ConnectivityChangeReceiver;
import com.websocket.client.util.Factory;

/**
 * This class is the main entry point for accessing QSocket.
 * <p/>
 * <p>
 * By creating a new {@link QSocket} instance and calling {@link
 * QSocket#connect()} a connection to QSocket is established.
 * </p>
 * <p/>
 * <p>
 * Subscriptions for data are represented by
 * {@link com.websocket.client.channel.Channel} objects, or subclasses thereof.
 * Subscriptions are created by calling {@link QSocket#subscribe(String)},
 * </p>
 */
public class QSocket implements Client, ConnectivityChangeReceiver.ConnectivityChangeListener {

    private final QSocketOptions QSocketOptions;
    private final InternalConnection connection;
    private final ChannelManager channelManager;
    private final Factory factory;
    private Context context;

    /**
     * Creates a new instance of QSocket.
     *
     * @param Context context for the QSocket client library to use.
     */
    public QSocket(Context context) {

        this(new QSocketOptions(), context);
    }

    /**
     * Creates a new instance of QSocket.
     * @param QSocketOptions Options for the QSocket client library to use.
     * @param Context context for the QSocket client library to use.
     */
    public QSocket(final QSocketOptions QSocketOptions, Context context) {

        this(QSocketOptions, new Factory(), context);
    }

    /**
     * Creates a new QSocket instance using the provided Factory, package level
     * access for unit tests only.
     */
    QSocket(final QSocketOptions QSocketOptions, final Factory factory, Context context) {

        if (QSocketOptions == null) {
            throw new IllegalArgumentException("QSocketOptions cannot be null");
        }

        this.QSocketOptions = QSocketOptions;
        this.factory = factory;
        connection = factory.getConnection(this.QSocketOptions);
        channelManager = factory.getChannelManager();
        channelManager.setConnection(connection);
        this.context = context;
    }

    /* Connection methods */

    /**
     * Gets the underlying {@link Connection} object that is being used by this
     * instance of {@linkplain QSocket}.
     *
     * @return The {@link Connection} object.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Connects to QSocket. Any {@link ConnectionEventListener}s that have
     * already been registered using the
     * {@link Connection#bind(ConnectionState, ConnectionEventListener)} method
     * will receive connection events.
     * <p/>
     * <p>Calls are ignored (a connection is not attempted) if the {@link Connection#getState()} is not {@link com.websocket.client.connection.ConnectionState#DISCONNECTED}.</p>
     */
    public void connect() {
        connect(null);
    }

    /**
     * Binds a {@link ConnectionEventListener} to the specified events and then
     * connects to QSocket. This is equivalent to binding a
     * {@link ConnectionEventListener} using the
     * {@link Connection#bind(ConnectionState, ConnectionEventListener)} method
     * before connecting.
     * <p/>
     * <p>Calls are ignored (a connection is not attempted) if the {@link Connection#getState()} is not {@link com.websocket.client.connection.ConnectionState#DISCONNECTED}.</p>
     *
     * @param eventListener    A {@link ConnectionEventListener} that will receive connection
     *                         events. This can be null if you are not interested in
     *                         receiving connection events, in which case you should call
     *                         {@link #connect()} instead of this method.
     * @param connectionStates An optional list of {@link ConnectionState}s to bind your
     *                         {@link ConnectionEventListener} to before connecting to
     *                         QSocket. If you do not specify any {@link ConnectionState}s
     *                         then your {@link ConnectionEventListener} will be bound to all
     *                         connection events. This is equivalent to calling
     *                         {@link #connect(ConnectionEventListener, ConnectionState...)}
     *                         with {@link ConnectionState#ALL}.
     * @throws IllegalArgumentException If the {@link ConnectionEventListener} is null and at least
     *                                  one connection state has been specified.
     */
    public void connect(final ConnectionEventListener eventListener, ConnectionState... connectionStates) {

        if (eventListener != null) {
            if (connectionStates.length == 0) {
                connectionStates = new ConnectionState[]{ConnectionState.ALL};
            }

            for (final ConnectionState state : connectionStates) {
                connection.bind(state, eventListener);
            }
        } else {
            if (connectionStates.length > 0) {
                throw new IllegalArgumentException(
                        "Cannot bind to connection states with a null connection event listener");
            }
        }
        registerConnectivityChangeReceiver();
        connection.connect();
    }

    /**
     * Disconnect from QSocket.
     * <p/>
     * <p>
     * Calls are ignored if the {@link Connection#getState()}, retrieved from {@link QSocket#getConnection}, is not
     * {@link com.websocket.client.connection.ConnectionState#CONNECTED}.
     * </p>
     */
    public void disconnect() {
        if (connection.getState() == ConnectionState.CONNECTED) {
            unregisterConnectivityChangeReceiver();
            connection.disconnect();
        }
    }

    /* Subscription methods */

    /**
     * Subscribes to a public {@link Channel}.
     * <p/>
     * Note that subscriptions should be registered only once with a QSocket
     * instance. Subscriptions are persisted over disconnection and
     * re-registered with the server automatically on reconnection. This means
     * that subscriptions may also be registered before connect() is called,
     * they will be initiated on connection.
     *
     * @param channelName The name of the {@link Channel} to subscribe to.
     * @return The {@link Channel} object representing your subscription.
     */
    public Channel subscribe(final String channelName) {
        return subscribe(channelName, null);
    }

    /**
     * Binds a {@link ChannelEventListener} to the specified events and then
     * subscribes to a public {@link Channel}.
     *
     * @param channelName The name of the {@link Channel} to subscribe to.
     * @param listener    A {@link ChannelEventListener} to receive events. This can be
     *                    null if you don't want to bind a listener at subscription
     *                    time, in which case you should call {@link #subscribe(String)}
     *                    instead of this method.
     * @param eventNames  An optional list of event names to bind your
     *                    {@link ChannelEventListener} to before subscribing.
     * @return The {@link Channel} object representing your subscription.
     * @throws IllegalArgumentException If any of the following are true:
     *                                  <ul>
     *                                  <li>The channel name is null.</li>
     *                                  <li>You are already subscribed to this channel.</li>
     *                                  <li>At least one of the specified event names is null.</li>
     *                                  <li>You have specified at least one event name and your
     *                                  {@link ChannelEventListener} is null.</li>
     *                                  </ul>
     */
    public Channel subscribe(final String channelName, final ChannelEventListener listener, final String... eventNames) {

        final InternalChannel channel = factory.newPublicChannel(channelName);
        channelManager.subscribeTo(channel, listener, eventNames);

        return channel;
    }


    /**
     * Unsubscribes from a channel using via the name of the channel.
     *
     * @param channelName the name of the channel to be unsubscribed from.
     */
    public void unsubscribe(final String channelName) {

        channelManager.unsubscribeFrom(channelName, null);
    }

    /**
     * Unsubscribes from a channel using via the name of the channel and Binds a {@link ChannelUnsubscriptionEventListener}.
     *
     * @param channelName
     * @param channelUnsubscriptionEventListeneristener
     */
    public void unsubscribe(final String channelName, final ChannelUnsubscriptionEventListener channelUnsubscriptionEventListeneristener) {

        channelManager.unsubscribeFrom(channelName, channelUnsubscriptionEventListeneristener);
    }


    /**
     * @param channelName The name of the public channel to be retrieved
     * @return A public channel, or null if it could not be found
     */
    public Channel getChannel(String channelName) {
        return channelManager.getChannel(channelName);
    }

    /**
     * callback for network connectivity availability
     */
    @Override
    public void onNetworkAvailable() {
        this.connect();
    }

    /**
     * Register listener for network state changes
     */
    private void registerConnectivityChangeReceiver() {
        context.registerReceiver(
                factory.getConnectivityChangeReceiver(this),
                new IntentFilter(
                        ConnectivityManager.CONNECTIVITY_ACTION));
    }

    /**
     * unregister listener for network state changes
     */
    private void unregisterConnectivityChangeReceiver() {
        context.unregisterReceiver(factory.getConnectivityChangeReceiver(this));
    }
}
