package com.grishberg.mobileserver.service.http;

/**
 * Created by grishberg on 08.01.16.
 */
import java.nio.channels.SocketChannel;

class ServerDataEvent {
    public NioServer server;
    public SocketChannel socket;
    public byte[] data;

    public ServerDataEvent(NioServer server, SocketChannel socket, byte[] data) {
        this.server = server;
        this.socket = socket;
        this.data = data;
    }
}