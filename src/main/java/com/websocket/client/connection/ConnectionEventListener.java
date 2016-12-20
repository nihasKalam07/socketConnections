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
package com.websocket.client.connection;

/**
 * Client applications should implement this interface if they wish to receive
 * notifications when the state of a {@link Connection} changes or an error is
 * thrown.
 *
 * <p>
 * Implementations of this interface can be bound to the com.websocket.client.connection by calling
 * {@link Connection#bind(ConnectionState, ConnectionEventListener)}. The
 * com.websocket.client.connection itself can be retrieved from the {@link com.websocket.client.QSocket}
 * object by calling {@link com.websocket.client.QSocket#getConnection()}.
 * </p>
 *
 * <p>
 * Alternatively, you can bind your implementation of the interface and connect
 * at the same time by calling
 * {@link com.websocket.client.QSocket#connect(ConnectionEventListener, ConnectionState...)}
 * .
 * </p>
 */
public interface ConnectionEventListener {

    /**
     * Callback that is fired whenever the {@link ConnectionState} of the
     * {@link Connection} changes. The state typically changes during com.websocket.client.connection
     * to QSocket and during disconnection and reconnection.
     *
     * <p>
     * This callback is only fired if the {@linkplain ConnectionEventListener}
     * has been bound to the new state by calling
     * {@link Connection#bind(ConnectionState, ConnectionEventListener)} with
     * either the new state or {@link ConnectionState#ALL}.
     * </p>
     *
     * @param change An object that contains the previous state of the com.websocket.client.connection
     *            and the new state. The new state can be retrieved by calling
     *            {@link ConnectionStateChange#getCurrentState()}.
     */
    void onConnectionStateChange(ConnectionStateChange change);

    /**
     * Callback that indicates either:
     * <ul>
     * <li>An error message has been received from QSocket, or</li>
     * <li>An error has occurred in the client library.</li>
     * </ul>
     *
     * <p>
     * All {@linkplain ConnectionEventListener}s that have been registered by
     * calling {@link Connection#bind(ConnectionState, ConnectionEventListener)}
     * will receive this callback, even if the
     * {@linkplain ConnectionEventListener} is only bound to specific com.websocket.client.connection
     * status changes.
     * </p>
     *
     * @param message
     *            A message indicating the cause of the error.
     * @param code
     *            The error code for the message. Can be null.
     * @param e
     *            The exception that was thrown, if any. Can be null.
     */
    void onError(String message, String code, Exception e);
}
