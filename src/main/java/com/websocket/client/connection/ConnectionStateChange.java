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
 * Represents a change in com.websocket.client.connection state.
 */
public class ConnectionStateChange {

    private final ConnectionState previousState;
    private final ConnectionState currentState;

    /**
     * Used within the library to create a com.websocket.client.connection state change. Not be used
     * used as part of the API.
     *
     * @param previousState The previous com.websocket.client.connection state
     * @param currentState The current com.websocket.client.connection state
     */
    public ConnectionStateChange(final ConnectionState previousState, final ConnectionState currentState) {

        if (previousState == currentState) {
            throw new IllegalArgumentException(
                    "Attempted to create an com.websocket.client.connection state update where both previous and current state are: "
                            + currentState);
        }

        this.previousState = previousState;
        this.currentState = currentState;
    }

    /**
     * The previous connections state. The state the com.websocket.client.connection has transitioned
     * from.
     *
     * @return The previous com.websocket.client.connection state
     */
    public ConnectionState getPreviousState() {
        return previousState;
    }

    /**
     * The current com.websocket.client.connection state. The state the com.websocket.client.connection has transitioned
     * to.
     *
     * @return The current com.websocket.client.connection state
     */
    public ConnectionState getCurrentState() {
        return currentState;
    }

    @Override
    public int hashCode() {
        return previousState.hashCode() + currentState.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj instanceof ConnectionStateChange) {
            final ConnectionStateChange other = (ConnectionStateChange)obj;
            return currentState == other.currentState && previousState == other.previousState;
        }

        return false;
    }
}
