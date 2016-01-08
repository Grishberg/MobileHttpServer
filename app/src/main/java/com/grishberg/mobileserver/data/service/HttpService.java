package com.grishberg.mobileserver.data.service;

import com.grishberg.mobileserver.framework.BaseBinderService;
import com.grishberg.mobileserver.service.http.HttpSocketServer;
import com.grishberg.mobileserver.service.http.IResponseListener;
import com.grishberg.mobileserver.service.http.NioServer;

import java.nio.channels.SocketChannel;

/**
 * Created by g on 07.01.16.
 */
public class HttpService extends BaseBinderService implements IResponseListener {
    private HttpSocketServer mServer;

    @Override
    public void onCreate() {
        super.onCreate();
        mServer = new HttpSocketServer(8080);
    }

    public void startServer(){
        mServer.startServer();
    }

    public void stopService(){
        mServer.stopServer();
    }

    @Override
    public void onResponse(SocketChannel responseChannel) {
        sendMessage(0,0);
    }
}
