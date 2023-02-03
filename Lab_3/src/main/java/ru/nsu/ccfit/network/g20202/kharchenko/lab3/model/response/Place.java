package ru.nsu.ccfit.network.g20202.kharchenko.lab3.model.response;

public class Place {

    String name;
    String lat;
    String lng;

    public Place(String name, String lat, String lng) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public String toString() {
        return name + " (" + lng + ", " + lat + ")";
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }
}
