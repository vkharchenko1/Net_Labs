package ru.nsu.ccfit.network.g20202.kharchenko.lab3.model.response;

public class Sight {
    String xid;
    String name;

    public Sight(String xid, String name) {
        this.xid = xid;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getXid() {
        return xid;
    }

}
