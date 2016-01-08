package com.grishberg.mobileserver.service.http;

import java.nio.channels.SocketChannel;

/**
 * Created by g on 07.01.16.
 */
public interface IResponseListener {
    void onResponse(SocketChannel responseChannel);
}
