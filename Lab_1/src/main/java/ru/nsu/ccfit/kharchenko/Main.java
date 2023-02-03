package ru.nsu.ccfit.kharchenko;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class Main {

    static int port = 60000;
    static String groupAddress = "230.0.0.0";

    public static void main(String[] args) throws IOException {

        //Создание мультикаст-сокета
        MulticastSocket multicastSocket = new MulticastSocket(port);
        InetAddress address = InetAddress.getByName(groupAddress);
        multicastSocket.joinGroup(new InetSocketAddress(address, port), null);

        //Создание всех работающих классов
        Manager manager = new Manager();
        Listener listener = new Listener(multicastSocket, manager);
        Sender sender = new Sender(address, port);

        //Создание всех потоков
        Thread listenerThread = new Thread(listener);
        Thread senderThread = new Thread(sender);
        Thread managerThread = new Thread(manager);

        //Запуск всех потоков
        listenerThread.start();
        senderThread.start();
        managerThread.start();

        //Ожидание ввода
        Scanner scanner = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Type anything to stop execution");
        scanner.nextLine();

        //Завершение всех потоков
        scanner.close();
        listener.stop();
        sender.stop();
        manager.stop();
    }

}