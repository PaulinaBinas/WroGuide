package com.example.wroguide;

public class Location {

    public String name;
    public String category;
    public double longitude;
    public double latitude;

    public Location() {

    }

    public Location(String name, String category, double latitude, double longitude) {
        this.name = name;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

}
