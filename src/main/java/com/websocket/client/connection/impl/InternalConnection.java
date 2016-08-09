package com.websocket.client.connection.impl;


import com.websocket.client.connection.Connection;

public interface InternalConnection extends Connection {

    void sendMessage(String message);

    void disconnect();
}
