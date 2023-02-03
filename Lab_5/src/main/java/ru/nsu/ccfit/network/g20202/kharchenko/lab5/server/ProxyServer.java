package ru.nsu.ccfit.network.g20202.kharchenko.lab5.server;

import java.io.IOException;
import java.net.*;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * This thread connects clients to the program and calls ClientHandler for each client.
 */

public class ProxyServer implements Runnable {

    private final int proxyPort;
    private ServerSocketChannel proxySocket;
    private Selector selector;
    private boolean isStopped = false;

    ProxyServer(int port) {
        proxyPort = port;
    }

    @Override
    public void run() {
        try {
            // Open selector
            selector = Selector.open();

            // Open server socket and add to selector
            proxySocket = ServerSocketChannel.open();
            proxySocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), proxyPort));
            proxySocket.configureBlocking(false);
            proxySocket.register(selector, SelectionKey.OP_ACCEPT);

            // Continuously work with channels
            while (!isStopped) {
                // Select available operations
                selector.select();

                // Iterate over available operations
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();

                    // Accept connections
                    if (key.isAcceptable()) {
                        // Accept new socket
                        SocketChannel clientSocket = proxySocket.accept();

                        // Create a new connection
                        if (clientSocket != null) {
                            ProxyConnection connection = new ProxyConnection(selector);
                            connection.setClient(clientSocket);
                        }
                    } else {
                        ((ProxyConnection)key.attachment()).handle(key);
                    }

                    iter.remove();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void stop() {
        try {
            proxySocket.close();
            selector.close();
            isStopped = true;
        } catch (IOException e) {
            System.out.println("IOException was encountered when closing the socket.");
        }
    }
}
