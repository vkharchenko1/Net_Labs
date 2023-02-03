package ru.nsu.ccfit.kharchenko;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class Listener implements Runnable {

    boolean isStopped = false;
    MulticastSocket multicastSocket;
    Manager manager;

    public Listener(MulticastSocket socket, Manager mngr) throws IOException {
        multicastSocket = socket;
        manager = mngr;
    }

    @Override
    public void run() {
        while (!isStopped) {
            byte[] buff = new byte[1024];
            DatagramPacket recvPacket = new DatagramPacket(buff, 1024);
            try {
                multicastSocket.receive(recvPacket);
                manager.addProgram((InetSocketAddress)recvPacket.getSocketAddress());
            } catch (IOException e) {
                System.out.println("Couldn't receive packet");
            }
        }
    }

    public void stop() {
        isStopped = true;
        Thread.currentThread().interrupt();
        multicastSocket.close();
    }

}
