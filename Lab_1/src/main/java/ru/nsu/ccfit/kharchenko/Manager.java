package ru.nsu.ccfit.kharchenko;

import java.net.InetSocketAddress;
import java.util.HashSet;
public class Manager implements Runnable {

    boolean isStopped = false;
    HashSet<InetSocketAddress> programsList = new HashSet<>();
    HashSet<InetSocketAddress> receivedList = new HashSet<>();

    @Override
    public void run() {
        while (!isStopped) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Manager sleep was interrupted");
                return;
            }
            if (!programsList.equals(receivedList)) {
                System.out.println(receivedList);
                programsList = receivedList;
            }
            receivedList = new HashSet<>();
        }
    }

    public void addProgram(InetSocketAddress program) {
        receivedList.add(program);
    }

    public void stop() {
        isStopped = true;
        Thread.currentThread().interrupt();
    }
}
