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
 * An object that represents a QSocket com.websocket.client.channel. An implementation of this
 * interface is returned when you call
 * {@link com.websocket.client.QSocket#subscribe(String)} or
 * {@link com.websocket.client.QSocket#subscribe(String, ChannelEventListener, String...)}
 * .
 *
 */
public interface Channel {

    /**
     * Gets the name of the QSocket com.websocket.client.channel that this object represents.
     *
     * @return The name of the com.websocket.client.channel.
     */
    String getName();

    /**
     * Binds a {@link SubscriptionEventListener} to an event. The
     * {@link SubscriptionEventListener} will be notified whenever the specified
     * event is received on this com.websocket.client.channel.
     *
     * @param eventName
     *            The name of the event to listen to.
     * @param listener
     *            A listener to receive notifications when the event is
     *            received.
     * @throws IllegalArgumentException
     *             If either of the following are true:
     *             <ul>
     *             <li>The name of the event is null.</li>
     *             <li>The {@link SubscriptionEventListener} is null.</li>
     *             </ul>
     * @throws IllegalStateException
     *             If the com.websocket.client.channel has been unsubscribed by calling
     *             {@link com.websocket.client.QSocket#unsubscribe(String)}. This
     *             puts the {@linkplain Channel} in a terminal state from which
     *             it can no longer be used. To resubscribe, call
     *             {@link com.websocket.client.QSocket#subscribe(String)} or
     *             {@link com.websocket.client.QSocket#subscribe(String, ChannelEventListener, String...)}
     *             again to receive a fresh {@linkplain Channel} instance.
     */
    void bind(String eventName, SubscriptionEventListener listener);

    /**
     * <p>
     * Unbinds a previously bound {@link SubscriptionEventListener} from an
     * event. The {@link SubscriptionEventListener} will no longer be notified
     * whenever the specified event is received on this com.websocket.client.channel.
     * </p>
     *
     * <p>
     * Calling this method does not unsubscribe from the com.websocket.client.channel even if there
     * are no more {@link SubscriptionEventListener}s bound to it. If you want
     * to unsubscribe from the com.websocket.client.channel completely, call
     * {@link com.websocket.client.QSocket#unsubscribe(String)}. It is not necessary
     * to unbind your {@link SubscriptionEventListener}s first.
     * </p>
     *
     * @param eventName
     *            The name of the event to stop listening to.
     * @param listener
     *            The listener to unbind from the event.
     * @throws IllegalArgumentException
     *             If either of the following are true:
     *             <ul>
     *             <li>The name of the event is null.</li>
     *             <li>The {@link SubscriptionEventListener} is null.</li>
     *             </ul>
     * @throws IllegalStateException
     *             If the com.websocket.client.channel has been unsubscribed by calling
     *             {@link com.websocket.client.QSocket#unsubscribe(String)}. This
     *             puts the {@linkplain Channel} in a terminal state from which
     *             it can no longer be used. To resubscribe, call
     *             {@link com.websocket.client.QSocket#subscribe(String)} or
     *             {@link com.websocket.client.QSocket#subscribe(String, ChannelEventListener, String...)}
     *             again to receive a fresh {@linkplain Channel} instance.
     */
    void unbind(String eventName, SubscriptionEventListener listener);

    /**
     *
     * @return Whether or not the com.websocket.client.channel is subscribed.
     */
    boolean isSubscribed();
}
