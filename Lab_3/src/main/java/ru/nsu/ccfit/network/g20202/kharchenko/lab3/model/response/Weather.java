package ru.nsu.ccfit.network.g20202.kharchenko.lab3.model.response;

public class Weather {
    String main;
    String desc;

    public Weather(String main, String desc) {
        this.main = main;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return main + ": " + desc + ".";
    }
}
