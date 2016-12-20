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
package com.websocket.client;


import com.websocket.client.channel.Channel;
import com.websocket.client.channel.ChannelEventListener;
import com.websocket.client.connection.Connection;
import com.websocket.client.connection.ConnectionEventListener;
import com.websocket.client.connection.ConnectionState;

public interface Client {
    Connection getConnection();
    void connect();
    void connect(final ConnectionEventListener eventListener, ConnectionState... connectionStates);
    void disconnect();
    Channel subscribe(final String channelName);
    Channel subscribe(final String channelName, final ChannelEventListener listener, final String... eventNames);
    void unsubscribe(final String channelName);
    Channel getChannel(String channelName);
}
