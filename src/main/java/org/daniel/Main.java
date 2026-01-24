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
import java.util.concurrent.*;

import static java.lang.System.exit;

public class Main {

    static public final String GEOAPI = "https://geocoding-api.open-meteo.com/v1/search?name=";
    static public final String WEATHERAPI = "https://api.open-meteo.com/v1/forecast?";


    private static HttpURLConnection connection;
    static int i = 0;
    static String cityname = "Gdansk";

    public static void main(String[] args) {

        Location city;
        WeatherSnapshot weather;
        List<WeatherSnapshot> snapshots = new ArrayList<>();
        int command;


        System.out.print("What do you want to do ? Choose from 1 to 5 and press Enter : \n"
                + "1 - Get the temperature for hardcoded city which is " + cityname + "\n"
                + "2 - Get the temperature for whatever city you like \n"
                + "3 - Get the temperature for hardcoded city and count delta \n"
                + "4 - Get historical data for hardcoded city and calculate delta \n"
                + "5 - Exit \n\r"
        );


        Scanner scanner = new Scanner(System.in);
        int option = scanner.nextInt();
        scanner.nextLine();

        switch (option) {
            case 1:
                city = getLocation(cityname);
                weather = getWeatcher(city);
                System.out.println("Temperature for city " + city.name + " is :" + weather.getCurrent().getTemperature2m());
                break;
            case 2:
                System.out.print("Enter city name to get actual temperature:");
                String newCity = scanner.nextLine();
                System.out.println("You typed: " + newCity);
                city = getLocation(newCity);
                System.out.println(city.name + " longitude: " + city.longitude + " latitude: " + city.latitude + " country: " + city.country);
                weather = getWeatcher(city);
                System.out.println("Temperature for city " + city.name + " is :" + weather.getCurrent().getTemperature2m());
                break;
            case 3:
                System.out.print("How long should I gather data in minutes ?");
                double stop = scanner.nextDouble();
                scanner.nextLine();
                city = getLocation(cityname);
                final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                final CountDownLatch latch = new CountDownLatch(1);
                executor.scheduleAtFixedRate(() -> {
                    System.out.println(++i);

                    snapshots.add(getWeatcher(city));

                    if (i > stop) {
                        latch.countDown();
                    }
                }, 0, 1, TimeUnit.MINUTES);
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                executor.shutdownNow();

                snapshots.stream().forEach(o -> System.out.println(o.getCurrent().getTemperature2m() + o.getCurrent().getTime()));


                break;
            case 4:
                System.out.println("TODO");
                break;
            case 5:
               System.exit(0);
            default:
                break;
        }


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

        GeoResponse loc;
        List<Location> locList;
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