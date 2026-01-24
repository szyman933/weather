package org.daniel;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoResponse {

    @JsonProperty("results")
    private List<Location> results;

    @JsonProperty("generationtime_ms")
    private double generationtimeMs;

    public List<Location> getResults() {
        return results;
    }
}
