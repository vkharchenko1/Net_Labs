package ru.nsu.ccfit.network.g20202.kharchenko.lab5.server;

import java.util.Scanner;

public class Main {

    private static final int proxyPort = 52345;

    public static void main(String[] args) {
        //Launch server on predefined port
        ProxyServer server = new ProxyServer(proxyPort);
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