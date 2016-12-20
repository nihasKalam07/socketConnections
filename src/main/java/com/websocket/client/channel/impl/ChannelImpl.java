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

import com.google.gson.Gson;
import com.websocket.client.channel.ChannelEventListener;
import com.websocket.client.channel.ChannelState;
import com.websocket.client.channel.ChannelUnsubscriptionEventListener;
import com.websocket.client.channel.SubscriptionEventListener;
import com.websocket.client.util.Constants;
import com.websocket.client.util.Factory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ChannelImpl implements InternalChannel {

    private static final Gson GSON = new Gson();
    private static final String SUBSCRIPTION_SUCCESS_EVENT = "103";
    private static final String UNSUBSCRIPTION_SUCCESS_EVENT = "104";
    private static final String INTERNAL_EVENT_PREFIX = "qsocket_internal:";
    protected final String name;
    private final Map<String, Set<SubscriptionEventListener>> eventNameToListenerMap = new HashMap<String, Set<SubscriptionEventListener>>();
    protected volatile ChannelState state = ChannelState.INITIAL;
    private ChannelEventListener eventListener;
    private ChannelUnsubscriptionEventListener channelUnsubscriptionEventListener;
    private final Factory factory;
    private final Object lock = new Object();

    public ChannelImpl(final String channelName, final Factory factory) {

        if (channelName == null) {
            throw new IllegalArgumentException("Cannot subscribe to a channel with a null name");
        }

        name = channelName;
        this.factory = factory;
    }

    /* Channel implementation */

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void bind(final String eventName, final SubscriptionEventListener listener) {

        validateArguments(eventName, listener);

        synchronized (lock) {
            Set<SubscriptionEventListener> listeners = eventNameToListenerMap.get(eventName);
            if (listeners == null) {
                listeners = new HashSet<SubscriptionEventListener>();
                eventNameToListenerMap.put(eventName, listeners);
            }
            listeners.add(listener);
        }
    }

    @Override
    public void unbind(final String eventName, final SubscriptionEventListener listener) {

        validateArguments(eventName, listener);

        synchronized (lock) {
            final Set<SubscriptionEventListener> listeners = eventNameToListenerMap.get(eventName);
            if (listeners != null) {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    eventNameToListenerMap.remove(eventName);
                }
            }
        }
    }

    @Override
    public boolean isSubscribed() {
        return state == ChannelState.SUBSCRIBED;
    }

    /* InternalChannel implementation */

    @Override
    public void onMessage(final String event, final String message) {

        if (event.equals(SUBSCRIPTION_SUCCESS_EVENT)) {
            updateState(ChannelState.SUBSCRIBED);
        }else if (event.equals(UNSUBSCRIPTION_SUCCESS_EVENT)) {
            updateState(ChannelState.UNSUBSCRIBED);
        }
        else {
            final String data = extractDataFrom(message);
            factory.queueOnEventThread(new Runnable() {
                @Override
                public void run() {
                    eventListener.onEvent(name, event, data);
                }
            });
//            final Set<SubscriptionEventListener> listeners;
//            synchronized (lock) {
//                final Set<SubscriptionEventListener> sharedListeners = eventNameToListenerMap.get(event);
//                if (sharedListeners != null) {
//                    listeners = new HashSet<SubscriptionEventListener>(sharedListeners);
//                }
//                else {
//                    listeners = null;
//                }
//            }
//
//            if (listeners != null) {
//                for (final SubscriptionEventListener listener : listeners) {
//                    final String data = extractDataFrom(message);
//
//                    factory.queueOnEventThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            listener.onEvent(name, event, data);
//                        }
//                    });
//                }
//            }
        }
    }

    @Override
    public String toSubscribeMessage() {

        final Map<Object, Object> jsonObject = new LinkedHashMap<Object, Object>();
        jsonObject.put(Constants.COMMAND, Constants.SUBSCRIBE);

        jsonObject.put(Constants.CHANNEL, name);

        return GSON.toJson(jsonObject);
    }

    @Override
    public String toUnsubscribeMessage() {
        final Map<Object, Object> jsonObject = new LinkedHashMap<Object, Object>();
        jsonObject.put(Constants.COMMAND, Constants.UNSUBSCRIBE);

        jsonObject.put(Constants.CHANNEL, name);

        return GSON.toJson(jsonObject);
    }

    @Override
    public void updateState(final ChannelState state) {

        this.state = state;

        if (state == ChannelState.SUBSCRIBED && eventListener != null) {
            factory.queueOnEventThread(new Runnable() {
                @Override
                public void run() {
                    eventListener.onSubscriptionSucceeded(ChannelImpl.this.getName());
                }
            });
        } else if (state == ChannelState.UNSUBSCRIBED && channelUnsubscriptionEventListener != null) {
            factory.queueOnEventThread(new Runnable() {
                @Override
                public void run() {
                    channelUnsubscriptionEventListener.onUnsubscribed(ChannelImpl.this.getName());
                }
            });
        }
    }

    /* Comparable implementation */

    @Override
    public void setEventListener(final ChannelEventListener listener) {
        eventListener = listener;
    }

    @Override
    public void setUnsubscribeEventListener(ChannelUnsubscriptionEventListener listener) {
        channelUnsubscriptionEventListener = listener;
    }

    @Override
    public ChannelEventListener getEventListener() {
        return eventListener;
    }

    @Override
    public int compareTo(final InternalChannel other) {
        return getName().compareTo(other.getName());
    }

    /* implementation detail */

    @Override
    public String toString() {
        return String.format("[Public Channel: name=%s]", name);
    }

    @SuppressWarnings("unchecked")
    private String extractDataFrom(final String message) {
//        final Map<Object, Object> jsonObject = GSON.fromJson(message, Map.class);
//        return (String)jsonObject.get("data");
        return message;
    }

    private void validateArguments(final String eventName, final SubscriptionEventListener listener) {

        if (eventName == null) {
            throw new IllegalArgumentException("Cannot bind or unbind to channel " + name + " with a null event name");
        }

        if (listener == null) {
            throw new IllegalArgumentException("Cannot bind or unbind to channel " + name + " with a null listener");
        }

        if (eventName.startsWith(INTERNAL_EVENT_PREFIX)) {
            throw new IllegalArgumentException("Cannot bind or unbind channel " + name
                    + " with an internal event name such as " + eventName);
        }

        if (state == ChannelState.UNSUBSCRIBED) {
            throw new IllegalStateException(
                    "Cannot bind or unbind to events on a channel that has been unsubscribed. Call Pusher.subscribe() to resubscribe to this channel");
        }
    }
}
