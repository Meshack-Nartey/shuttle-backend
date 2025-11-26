package com.shuttlebackend.eta.model;

public class Waypoint {
    private double lat;
    private double lon;

    public Waypoint() {}
    public Waypoint(double lat, double lon) { this.lat = lat; this.lon = lon; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLon(double lon) { this.lon = lon; }
}

