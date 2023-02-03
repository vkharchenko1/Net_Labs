package ru.nsu.ccfit.network.g20202.kharchenko.lab5.server;

import org.xbill.DNS.Address;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;


public class ProxyConnection {
    private static final int BUFFER_SIZE = 4096;

    // SOCKS byte meanings
    private static final byte SOCKS_VER_5 = (byte)0x05;
    private static final byte AUTH_NONE = (byte)0x00;
    private static final byte AUTH_ERR = (byte)0xFF;
    private static final byte TCP_STREAM = (byte)0x01;
    private static final byte CONNECTION_TYPE_NOT_SUPPORTED = (byte)0x07;
    private static final byte ADDR_TYPE_IPv4 = (byte)0x01;
    private static final byte ADDR_TYPE_DOMAIN_NAME = (byte)0x03;
    private static final byte REQUEST_GRANTED = (byte)0x00;
    private static final byte CONNECTION_ERROR = (byte)0x01;
    private static final byte ADDRESS_TYPE_NOT_SUPPORTED = (byte)0x08;

    private SocketChannel clientSocket;
    private SocketChannel serverSocket;
    private Selector selector;

    private SelectionKey clientKey;
    private SelectionKey serverKey;

    private ByteBuffer clientReadBuffer = null;
    private ByteBuffer clientWriteBuffer = null;
    private ByteBuffer serverReadBuffer = null;
    private ByteBuffer serverWriteBuffer = null;

    private State state;

    public ProxyConnection(Selector selector) {
        this.selector = selector;
        state = State.GREETING;
    }

    public void setClient(SocketChannel clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        clientSocket.configureBlocking(false);

        // Register client for operations
        clientKey = clientSocket.register(selector, SelectionKey.OP_READ);

        // Attach connection to the returned selection key
        clientKey.attach(this);
    }

    public void handle(SelectionKey key) {
        if (state == State.FAILURE) {
            closeClient();
            closeServer();
            return;
        }
        SelectableChannel socketChannel = key.channel();
        try {
            if (socketChannel == clientSocket) {
                if (key.isReadable()) {
                    switch (state) {
                        case GREETING:
                            greetClient();
                            System.out.println("Greeting from client");
                            break;
                        case CONNECTING:
                            connectToServer();
                            break;
                        case TRANSLATING:
                            readClient();
                            if (clientKey.isValid() && serverKey.isValid()) {
                                clientKey.interestOpsAnd(~SelectionKey.OP_READ & clientSocket.validOps());
                                serverKey.interestOpsOr(SelectionKey.OP_WRITE);
                            }
                            break;
                    }
                } else if (key.isWritable()) {
                    writeClient();
                    switch (state) {
                        case GREETING:
                            System.out.println("Greeting answered");
                            state = State.CONNECTING;
                            clientKey.interestOps(SelectionKey.OP_READ);
                            break;
                        case CONNECTING:
                            state = State.TRANSLATING;
                            clientKey.interestOps(SelectionKey.OP_READ);
                            break;
                        case TRANSLATING:
                            clientKey.interestOpsAnd(~SelectionKey.OP_WRITE & clientSocket.validOps());
                            serverKey.interestOpsOr(SelectionKey.OP_READ);
                            break;
                    }
                }
            } else if (socketChannel == serverSocket) {
                if (key.isReadable()) {
                    readServer();
                    if (clientKey.isValid() && serverKey.isValid()) {
                        serverKey.interestOpsAnd(~SelectionKey.OP_READ & serverSocket.validOps());
                        clientKey.interestOpsOr(SelectionKey.OP_WRITE);
                    }
                } else if (key.isWritable()) {
                    writeServer();
                    if (clientKey.isValid() && serverKey.isValid()) {
                        serverKey.interestOpsAnd(~SelectionKey.OP_WRITE & serverSocket.validOps());
                        clientKey.interestOpsOr(SelectionKey.OP_READ);
                    }
                }
            }
        } catch (IOException ex) {
            closeServer();
            closeClient();
            ex.printStackTrace();
        }
    }

    private void greetClient() throws IOException {
        // Reallocate buffer
        clientReadBuffer = ByteBuffer.allocate(BUFFER_SIZE);

        // Read greeting message
        int length = clientSocket.read(clientReadBuffer);

        if (length == -1) {
            state = State.FAILURE;
            return;
        }

        // Convert message to byte array
        byte[] msg = clientReadBuffer.array();

        // Create answer
        byte[] answer = new byte[2];

        // Set SOCKS versions to 5
        answer[0] = SOCKS_VER_5;

        // Set error message
        answer[1] = AUTH_ERR;

        // Check whether client is using SOCKS 5 protocol
        if (clientReadBuffer.get(0) == 5) {
            for (int i = 2; i < msg[1] + 2; i++) {
                if (msg[i] == AUTH_NONE) {
                    // Set authentication method to NONE
                    answer[1] = AUTH_NONE;
                    break;
                }
            }
        }

        if (answer[1] == AUTH_ERR) {
            state = State.FAILURE;
        }

        System.out.println("Auth method: " + answer[1]);

        // Set answer as a buffer from which to write to client
        clientWriteBuffer = ByteBuffer.wrap(answer);

        // Select client socket for writing
        clientKey.interestOps(SelectionKey.OP_WRITE);
    }

    private void connectToServer() throws IOException {
        // Reallocate buffer
        clientReadBuffer = ByteBuffer.allocate(BUFFER_SIZE);

        // Read greeting message
        int length = clientSocket.read(clientReadBuffer);

        if (length == -1) {
            state = State.FAILURE;
            return;
        }

        // Convert message to byte array
        byte[] msg = clientReadBuffer.array();

        // Create answer
        byte[] answer = new byte[length];

        System.arraycopy(msg, 0, answer, 0, length);

        // Connect to the server and write client a message according to results
        if (msg[0] != SOCKS_VER_5 ||  msg[1] != TCP_STREAM) {
            state = State.FAILURE;
            answer[1] = CONNECTION_TYPE_NOT_SUPPORTED;
        } else {
            if (msg[3] == ADDR_TYPE_IPv4) {
                // Read ip and port
                byte[] ip = new byte[4];
                System.arraycopy(msg, 4, ip, 0, 4);
                int port = ((msg[8] & 0xFF) << 8) | (msg[9] & 0xFF);

                try {
                    // Open server connection
                    serverSocket = SocketChannel.open(new InetSocketAddress(InetAddress.getByAddress(ip), port));
                    serverSocket.configureBlocking(false);
                    serverKey = serverSocket.register(selector, SelectionKey.OP_READ, this);

                    answer[1] = REQUEST_GRANTED;
                } catch (IOException ex) {
                    state = State.FAILURE;
                    answer[1] = CONNECTION_ERROR;
                }
            } else if (msg[3] == ADDR_TYPE_DOMAIN_NAME) {
                // Get domain name length in bytes
                int addrlen = msg[4];

                // Get domain name from message
                byte[] name = new byte[addrlen];
                System.arraycopy(msg, 5, name, 0, addrlen);

                try {
                    // Get address and port
                    InetAddress address = Address.getByName(new String(name));
                    byte[] ip = address.getAddress();
                    int port = ((msg[5+addrlen] & 0xff) << 8) | (msg[6+addrlen] & 0xff);

                    // Open server connection
                    serverSocket = SocketChannel.open(new InetSocketAddress(InetAddress.getByAddress(ip), port));
                    serverSocket.configureBlocking(false);
                    serverKey = serverSocket.register(selector, SelectionKey.OP_READ, this);

                    System.out.println("Connected to server " + new String(name) + ":" + port);
                    System.out.println(address.toString() + ":" + port);

                    answer[1] = REQUEST_GRANTED;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    state = State.FAILURE;
                    answer[1] = CONNECTION_ERROR;
                }
            } else {
                state = State.FAILURE;
                answer[1] = ADDRESS_TYPE_NOT_SUPPORTED;
            }
        }

        if (state == State.FAILURE) {
            System.out.println("Failed to connect");
        }

        // Set answer as a buffer from which to write to client
        clientWriteBuffer = ByteBuffer.wrap(answer);

        // Select client socket for writing
        clientKey.interestOps(SelectionKey.OP_WRITE);
    }

    private void readClient() throws IOException {
        // Reallocate buffer
        clientReadBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        try {
            int length = clientSocket.read(clientReadBuffer);
            if (length == -1) {
                closeClient();
            } else if (length > 0) {
                byte[] write = new byte[length];
                System.arraycopy(clientReadBuffer.array(), 0, write, 0, length);
                serverWriteBuffer = ByteBuffer.wrap(write);
            }
        } catch (IOException ex) {
            state = State.FAILURE;
            closeClient();
        }
    }

    private void readServer() {
        // Reallocate buffer
        serverReadBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        try {
            int length = serverSocket.read(serverReadBuffer);
            if (length == -1) {
                closeServer();
            } else if (length > 0) {
                byte[] write = new byte[length];
                System.arraycopy(serverReadBuffer.array(), 0, write, 0, length);
                clientWriteBuffer = ByteBuffer.wrap(write);
            }
        } catch (IOException ex) {
            state = State.FAILURE;
            closeServer();
        }
    }

    private void writeClient() {
        try {
            clientSocket.write(clientWriteBuffer);
        } catch (IOException ex) {
            state = State.FAILURE;
            closeClient();
        }
    }

    private void writeServer() throws IOException {
        try {
            if (serverWriteBuffer == null)
                System.out.println("serverWriteBuffer is null");
            if (serverSocket == null)
                System.out.println("serverSocket is null");
            serverSocket.write(serverWriteBuffer);
        } catch (IOException ex) {
            state = State.FAILURE;
            closeServer();
        }
    }

    private void closeClient() {
        if(clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (clientKey != null) {
            clientKey.cancel();
        }
    }

    private void closeServer() {
        if(serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (serverKey != null) {
            serverKey.cancel();
        }
    }
    private enum State {
        GREETING,
        CONNECTING,
        TRANSLATING,
        FAILURE
    }
}
