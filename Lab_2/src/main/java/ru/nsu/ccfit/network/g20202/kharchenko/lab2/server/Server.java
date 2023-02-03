package ru.nsu.ccfit.network.g20202.kharchenko.lab2.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * This thread connects clients to the program ыand calls ClientHandler for each client.
 */

public class Server implements Runnable {

    private final int serverPort;
    private ServerSocket serverSocket;
    boolean isStopped = false;

    Server(int port) {
        serverPort = port;
    }

    @Override
    public void run() {
        try {
            //Open server socket
            serverSocket = new ServerSocket(serverPort);
            Socket clientSocket;

            //Wait for connection requests
            while (!isStopped) {
                clientSocket = serverSocket.accept();

                //Call client handler
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread clientHandlerThread = new Thread(clientHandler);
                clientHandlerThread.start();
            }
        } catch (SocketException ex) {
            System.out.println("Server socket closed.");
        } catch (IOException ex) {
            System.out.println("IOException was encountered while running the server.");
            ex.printStackTrace();
        }
    }

    public void stop() {
        try {
            serverSocket.close();
            isStopped = true;
        } catch (IOException e) {
            System.out.println("IOException was encountered when closing the socket.");
        }
    }
}
