package com.example.wroguide;

public class Location {

    public String name;
    public String category;
    public Long longitude;
    public Long latitude;

    public Location() {

    }

    public Location(String name, String category, Long latitude, Long longitude) {
        this.name = name;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
    }


}
