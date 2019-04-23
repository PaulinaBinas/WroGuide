package com.example.wroguide;

public class Location {

    private String name;
    private Category category;
    private double longitude;
    private double latitude;
    private String description;

    public Location(String name, Category category, double latitude, double longitude, String description) {
        this.name = name;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
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

    public String getDescription() { return description; }

}
