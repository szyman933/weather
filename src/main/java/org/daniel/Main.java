package org.daniel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Main {

    static public final String GEOAPI = "https://geocoding-api.open-meteo.com/v1/search?name=";
    static public final String WEATHERAPI = "https://api.open-meteo.com/v1/forecast?";

    private static HttpURLConnection connection;


    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter city name to get actual temperature: ");
        String input = scanner.nextLine();
        System.out.println("You typed: " + input);

        Location city = getLocation(input);

        WeatherSnapshot weather = getWeatcher(city);

        System.out.println("Temperature for city " + city.name + " is :" + weather.getCurrent().getTemperature2m());


    }


    public static String getResponse(String destination) {
        HttpURLConnection connection = null;
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();

        try {

            URL url = new URL(destination);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();
           // System.out.println(status);

            if (status > 299) {

                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            } else {

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }

            }

          //  System.out.println(responseContent.toString());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }


        return responseContent.toString();

    }


    public static Location getLocation(String cityName) {


        ObjectMapper objectMapper = new ObjectMapper();

        String response = getResponse(GEOAPI + cityName);

        GeoResponse loc = null;
        List<Location> locList = new ArrayList<>();
        try {
            loc = objectMapper.readValue(response, GeoResponse.class);

            locList = loc.getResults();

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return locList.get(0);
    }


    public static WeatherSnapshot getWeatcher(@NonNull Location desiredLocation) {


        ObjectMapper objectMapper = new ObjectMapper();

        String response = getResponse(WEATHERAPI + "latitude=" + desiredLocation.getLatitude() + "&longitude=" + desiredLocation.getLongitude() + "&current=temperature_2m");


        WeatherSnapshot ws = null;
        try {
            ws = objectMapper.readValue(response, WeatherSnapshot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return ws;
    }


}