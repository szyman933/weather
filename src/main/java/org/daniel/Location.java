package org.daniel;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {

    public Location() {
    }

    public double latitude;

    public double longitude;

    public String name;

    public double elevation;

    public String country_code;

    public String country;

    @JsonProperty("latitude")
    public double getLatitude() {
        return latitude;
    }
    @JsonProperty("longitude")
    public double getLongitude() {
        return longitude;
    }
    @JsonProperty("name")
    public String getName() {
        return name;
    }
    @JsonProperty("elevation")
    public double getElevation() {
        return elevation;
    }
    @JsonProperty("country_code")
    public String getCountry_code() {
        return country_code;
    }
    @JsonProperty("country")
    public String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return "Location{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", name='" + name + '\'' +
                ", elevation=" + elevation +
                ", country_code='" + country_code + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}

