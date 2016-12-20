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
