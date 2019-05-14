package com.example.wroguide;

public class Location {

    private String id;
    private String name;
    private Category category;
    private double longitude;
    private double latitude;
    private String description;

    public Location(String id, String name, Category category, double latitude, double longitude, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }

    public Location(String name, double latitude, double longitude) {

        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() { return id; }

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
