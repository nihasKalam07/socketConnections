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
