package ru.nsu.ccfit.network.g20202.kharchenko.lab2.server;

import java.util.Scanner;

/**
 * This thread creates the server and receives the input to terminate it.
 */

public class Main {

    private static final int serverPort = 63000;

    public static void main(String[] args) {
        //Launch server on predefined port
        Server server = new Server(serverPort);
        Thread serverThread = new Thread(server);
        serverThread.start();

        //Wait for input
        Scanner scanner = new Scanner(System.in);
        System.out.println("Type anything to stop execution");
        scanner.nextLine();
        scanner.close();

        //Close the server
        server.stop();
    }

}