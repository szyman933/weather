package org.daniel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherHistorical {

    private double latitude;
    private double longitude;

    private HourlyUnits hourly_units;
    private Hourly hourly;

    // Getters and setters
    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HourlyUnits {
        @JsonProperty("time")
        private String time;
        @JsonProperty("temperature_2m")
        private String temperature_2m;

        // Getters and setters
    }
    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hourly {
        @JsonProperty("time")
        private List<String> time;
        @JsonProperty("temperature_2m")
        private List<Double> temperature_2m;

        // Getters and setters
    }
}

