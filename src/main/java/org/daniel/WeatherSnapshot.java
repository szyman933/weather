package org.daniel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherSnapshot {

    public WeatherSnapshot() {
    }

    public WeatherSnapshot(double latitude, double longitude, double generationtimeMs, String timezone, double elevation, CurrentUnits currentUnits, Current current) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.generationtimeMs = generationtimeMs;
        this.currentUnits = currentUnits;
        this.current = current;
    }

    @JsonProperty("latitude")
    private double latitude;
    @JsonProperty("longitude")
    private double longitude;

    @JsonProperty("generationtime_ms")
    private double generationtimeMs;

    @JsonProperty("current_units")
    private CurrentUnits currentUnits;
    @JsonProperty("current")
    private Current current;

    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentUnits {

        private String time;
        private String interval;

        @JsonProperty("temperature_2m")
        private String temperature2m;


    }

    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Current {

        @JsonProperty("time")
        private String time;

        @JsonProperty("temperature_2m")
        private double temperature2m;

        @Override
        public String toString() {
            return "Current{" +
                    "time='" + time + '\'' +
                    ", temperature2m=" + temperature2m +
                    '}';
        }
    }


    @Override
    public String toString() {
        return " WeatherSnapshot{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", current=" + current +
                '}';
    }
}
