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
