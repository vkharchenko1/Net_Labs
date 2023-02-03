package ru.nsu.ccfit.network.g20202.kharchenko.lab4.utils;

import java.util.Random;

public enum SnakeDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    public static SnakeDirection randomDirection()  {
        return SnakeDirection.values()[new Random().nextInt(SnakeDirection.values().length)];
    }
}
