package ru.nsu.ccfit.network.g20202.kharchenko.lab2.server;

import java.util.TimerTask;

public class SpeedMeter extends TimerTask {

    private long totalByteCount = 0;
    private long lastIntervalByteCount = 0;
    private final long totalTimeStart;
    private long lastIntervalTimeStart;

    public SpeedMeter() {
        totalTimeStart = System.nanoTime();
        lastIntervalTimeStart = System.nanoTime();
    }

    @Override
    public void run() {
        long timeEnd = System.nanoTime();

        double totalTime = (double)(timeEnd - totalTimeStart) / 1000000000;
        double lastIntervalTime = (double)(timeEnd - lastIntervalTimeStart) / 1000000000;
        lastIntervalTimeStart = System.nanoTime();

        //Bytes per second
        double totalSpeed = (double)totalByteCount / totalTime;
        double lastIntervalSpeed = (double)lastIntervalByteCount / lastIntervalTime;

        System.out.println("Total speed: " + (long)totalSpeed + " bps");
        System.out.println("Speed for last " + lastIntervalTime + " seconds: " + (long)lastIntervalSpeed + " bps");

        lastIntervalByteCount = 0;
    }

    public void addReadCount(long readCount) {
        totalByteCount += readCount;
        lastIntervalByteCount += readCount;
    }
}
