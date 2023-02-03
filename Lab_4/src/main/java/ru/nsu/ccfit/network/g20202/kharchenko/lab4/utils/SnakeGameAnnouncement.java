package ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils;

import java.net.InetAddress;
import java.util.HashMap;

public class SnakeGameAnnouncement {
    public String name;
    public InetAddress address;
    public int port;

    public boolean canJoin;

    public SnakeGameParameters gameParameters;
    public HashMap<Integer, SnakePlayer> players;

    public void setAddress(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }
}
