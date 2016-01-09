package com.grishberg.mobileserver.service.http;

import com.grishberg.mobileserver.service.http.servlet.InfoServlet;
import com.grishberg.mobileserver.service.http.servlet.ListServlet;

import org.apache.http.*;
import org.apache.http.impl.nio.bootstrap.ServerBootstrap;

/**
 * Embedded HTTP/1.1 file server based on a non-blocking I/O model and capable of direct channel
 * (zero copy) data transfer.
 */
public class HttpSocketServer extends SimpleHttpServer {
    public HttpSocketServer(int port) {
        super(port);
    }

    @Override
    public void registerHandlers(ServerBootstrap serverBootstrap) {
        ListServlet listServlet = new ListServlet();
        serverBootstrap.registerHandler("/", listServlet);
        serverBootstrap.registerHandler("/list", listServlet);
        serverBootstrap.registerHandler("/info", new InfoServlet());
    }
}