package ru.nsu.ccfit.kharchenko;

import java.io.IOException;
import java.net.*;

public class Sender implements Runnable {

    boolean isStopped = false;
    DatagramSocket sendSocket;

    InetAddress mcAddress;
    int port;

    public Sender(InetAddress addr, int p) throws IOException {
        sendSocket = new DatagramSocket();
        mcAddress = addr;
        port = p;
    }

    @Override
    public void run() {
        while (!isStopped) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("Sender sleep was interrupted");
                return;
            }
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, 1024, mcAddress, port);
            try {
                sendSocket.send(packet);
            } catch (IOException e) {
                System.out.println("Failed to send a packet");
            }
        }
    }

    public void stop() {
        isStopped = true;
        Thread.currentThread().interrupt();
        sendSocket.close();
    }

}
