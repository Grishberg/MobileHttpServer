package com.grishberg.mobileserver.service.http;

import java.io.IOException;

import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;

import org.apache.http.*;
import org.apache.http.impl.nio.bootstrap.HttpServer;
import org.apache.http.impl.nio.bootstrap.ServerBootstrap;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.ssl.SSLContexts;

/**
 * Embedded HTTP/1.1 file server based on a non-blocking I/O model and capable of direct channel
 * (zero copy) data transfer.
 */
public class HttpSocketServer extends Thread {
    private final int mPort;
    private Thread mConnectionThread;
    private HttpServer mServer;

    public HttpSocketServer(int port) {
        mPort = port;
    }

    public boolean startServer() {
        start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (mServer != null) {
                    mServer.shutdown(5, TimeUnit.SECONDS);
                    mServer = null;
                    System.out.println("bye ;)");
                }
            }
        });
        return true;
    }

    public void stopServer() {
        if (mConnectionThread != null) {
            mConnectionThread.interrupt();
        }
        if (mServer != null) {
            mServer.shutdown(5, TimeUnit.SECONDS);
        }
        System.out.println("stopping server...");
    }

    @Override
    public void run() {
        super.run();

        SSLContext sslcontext = null;
        if (mPort == 8443) {
            // Initialize SSL context
            URL url = HttpSocketServer.class.getResource("/my.keystore");
            if (url == null) {
                System.out.println("Keystore not found");
                System.exit(1);
            }
            try {
                sslcontext = SSLContexts.custom()
                        .loadKeyMaterial(url, "secret".toCharArray(), "secret".toCharArray())
                        .build();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        IOReactorConfig config = IOReactorConfig.custom()
                .setSoTimeout(15000)
                .setTcpNoDelay(true)
                .build();

        mServer = ServerBootstrap.bootstrap()
                .setListenerPort(mPort)
                .setServerInfo("Test/1.1")
                .setIOReactorConfig(config)
                .setSslContext(sslcontext)
                .setExceptionLogger(ExceptionLogger.STD_ERR)
                .registerHandler("*", new ClientSessionWrapper())
                .create();

        try {
            mServer.start();
            mServer.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            if (mServer != null) {
                mServer.shutdown(5, TimeUnit.SECONDS);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}