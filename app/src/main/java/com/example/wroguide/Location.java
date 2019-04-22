package com.example.wroguide;

public class Location {

    private String name;
    private Category category;
    private double longitude;
    private double latitude;

    public Location(String name, Category category, double latitude, double longitude) {
        this.name = name;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

}
