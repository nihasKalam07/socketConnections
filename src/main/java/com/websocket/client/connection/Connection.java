package com.websocket.client.connection;

/**
 * Represents a com.websocket.client.connection to QSocket.
 *
 */
public interface Connection {

    /**
     * No need to call this via the API. Instead use {@link com.websocket.client.QSocket#connect}.
     */
    void connect();

    /**
     * Bind to com.websocket.client.connection events.
     *
     * @param state
     *            The states to bind to.
     * @param eventListener
     *            A listener to be called when the state changes.
     */
    void bind(ConnectionState state, ConnectionEventListener eventListener);

    /**
     * Unbind from com.websocket.client.connection state changes.
     *
     * @param state
     *            The state to unbind from.
     * @param eventListener
     *            The listener to be unbound.
     * @return <code>true</code> if the unbind was successful, otherwise
     *         <code>false</code>.
     */
    boolean unbind(ConnectionState state, ConnectionEventListener eventListener);

    /**
     * Gets the current com.websocket.client.connection state.
     *
     * @return The state.
     */
    ConnectionState getState();

}
