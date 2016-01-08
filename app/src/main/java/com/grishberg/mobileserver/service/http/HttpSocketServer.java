package com.grishberg.mobileserver.service.http;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class HttpSocketServer extends Thread {
    private static final String TAG = HttpSocketServer.class.getSimpleName();
    /**
     * ServerSocketChannel represents a channel for sockets that listen to
     * incoming connections.
     *
     * @throws IOException
     */
    private static String clientChannel = "clientChannel";
    private static String serverChannel = "serverChannel";
    private static String channelType = "channelType";
    private final int mPort;
    private final IResponseListener mListener;

    public HttpSocketServer(int port, IResponseListener listener) {
        mPort = port;
        mListener = listener;
    }

    public boolean startServer() {
        start();
        return true;
    }

    public void stopServer() {
        System.out.println("stopping server...");
        interrupt();
    }

    @Override
    public void run() {
        String localhost = "localhost";
        try {
            // create a new serversocketchannel. The channel is unbound.
            ServerSocketChannel channel = ServerSocketChannel.open();

            channel.socket().bind(new InetSocketAddress(localhost, mPort));
            channel.configureBlocking(false);

            Selector selector = Selector.open();
            SelectionKey socketServerSelectionKey = channel.register(selector,
                    SelectionKey.OP_ACCEPT);
            // set property in the key that identifies the channel
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(channelType, serverChannel);
            socketServerSelectionKey.attach(properties);
            // wait for the selected keys
            StringBuilder sb = new StringBuilder();

            for (; ; ) {
                if (selector.select() == 0) {
                    continue;
                }
                // the select method returns with a list of selected keys
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (((Map) key.attachment()).get(channelType).equals(
                            serverChannel)) {
                        // a new connection has been obtained. This channel is
                        // therefore a socket server.
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel clientSocketChannel = serverSocketChannel.accept();

                        if (clientSocketChannel != null) {
                            // set the client connection to be non blocking
                            clientSocketChannel.configureBlocking(false);
                            SelectionKey clientKey = clientSocketChannel.register(
                                    selector, SelectionKey.OP_READ,
                                    SelectionKey.OP_WRITE);
                            Map<String, String> clientproperties = new HashMap<String, String>();
                            clientproperties.put(channelType, clientChannel);
                            clientKey.attach(clientproperties);

                            // write something to the new created client
                            CharBuffer buffer = CharBuffer.wrap("Hello client");
                            if (mListener != null) {
                                mListener.onResponse(clientSocketChannel);
                            }
                            while (buffer.hasRemaining()) {
                                clientSocketChannel.write(Charset.defaultCharset()
                                        .encode(buffer));
                            }
                            buffer.clear();
                        }
                    } else {
                        ByteBuffer buffer = ByteBuffer.allocate(128);
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        int bytesRead = 0;
                        if (key.isReadable()) {
                            // the channel is non blocking so keep it open till the
                            // count is >=0
                            if ((bytesRead = clientChannel.read(buffer)) > 0) {
                                buffer.flip();
                                sb.append(buffer.array());
                                buffer.clear();
                            }
                            if (bytesRead < 0) {
                                // the key is automatically invalidated once the
                                // channel is closed
                                Log.d(TAG, sb.toString());
                                sb = new StringBuilder();
                                clientChannel.close();
                            }
                        }
                    }

                    // once a key is handled, it needs to be removed
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }
    //-------------------------------------------------------
}