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

public class Main {

    static public final String GEOAPI = "https://geocoding-api.open-meteo.com/v1/search?name=";
    static public final String WEATHERAPI = "https://api.open-meteo.com/v1/forecast?";
    static public final String WEATHERHISTORICAL = "https://api.open-meteo.com/v1/forecast?latitude=54.3523&longitude=18.6491&hourly=temperature_2m&timezone=Europe%2FMoscow&past_days=2&forecast_days=1";


    private static HttpURLConnection connection;
    static int i = 0;
    static String cityname = "Gdansk";

    public static void main(String[] args) {

        Location city;
        WeatherSnapshot weather;
        List<WeatherSnapshot> snapshots = new ArrayList<>();
        int command;

        do {


            System.out.print("\n\nWhat do you want to do ? Choose from 1 to 5 and press Enter : \n"
                    + "1 - Get the temperature for hardcoded city which is " + cityname + "\n"
                    + "2 - Get the temperature for whatever city you like \n"
                    + "3 - Start collecting temperature for hardcoded city and calculate trend \n"
                    + "4 - Get historical data for hardcoded city and calculate trend \n"
                    + "5 - Exit \n\r"
            );


            Scanner scanner = new Scanner(System.in);
            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    city = getLocation(cityname);
                    weather = getWeather(city);
                    System.out.println("Temperature for city " + city.name + " is :" + weather.getCurrent().getTemperature2m());
                    break;
                case 2:
                    System.out.print("Enter city name to get actual temperature:");
                    String newCity = scanner.nextLine();
                    System.out.println("You typed: " + newCity);
                    city = getLocation(newCity);
                    System.out.println(city.name + " longitude: " + city.longitude + " latitude: " + city.latitude + " country: " + city.country);
                    weather = getWeather(city);
                    System.out.println("Temperature for city " + city.name + " is :" + weather.getCurrent().getTemperature2m());
                    break;
                case 3:
                    System.out.print("How long should I gather data in minutes?\n");
                    System.out.print("Warning! Everything less than 30 minutes doesn't make sens because API data refresh interval is 15minutes\n");
                    double stop = scanner.nextDouble();
                    scanner.nextLine();
                    Location finalCity = getLocation(cityname);
                    //starting periodic task for collecting the temp measurements
                    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                    final CountDownLatch latch = new CountDownLatch(1);

                    executor.scheduleAtFixedRate(() -> {
                        System.out.println(++i);
                        snapshots.add(getWeather(finalCity));
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
                    // calculating trend
                    List<Double> data = new ArrayList<>();
                    snapshots.stream().forEach(o -> data.add(o.getCurrent().getTemperature2m()));
                    System.out.println(calculateTrend(data));
                    break;
                case 4:

                    WeatherHistorical wh;

                    wh = getWeatherHistoricalData();

                    List<Double> lista = wh.getHourly().getTemperature_2m();

                    System.out.println(calculateTrend(lista) + " in Gdansk.");

                    break;
                case 5:
                    System.exit(0);
                default:
                    break;
            }


        } while (true);

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
            connection.setConnectTimeout(90000);
            connection.setReadTimeout(90000);


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
        //assumption that always best choice is at 1st position

        if (locList == null) {
            System.out.println("Can't find any Location called: " + cityName + ", so will check weather in hardcoded Location\n");
            Location defaultLoc = new Location(18.6491, 54.3523, "Gdansk");
            return defaultLoc;

        }

        return locList.get(0);
    }

    public static WeatherSnapshot getWeather(@NonNull Location desiredLocation) {


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

    public static String calculateTrend(List<Double> list) {

        double[] table = new double[list.size()];

        for (int i = 0; i < list.size(); i++) {
            table[i] = list.get(i);
        }

        int n = table.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += table[i];
            sumXY += i * table[i];
            sumXX += i * i;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);

        if (slope > 0) {
            return "Getting warmer";
        } else if (slope < 0) {
            return "Getting colder";
        } else if (slope == 0) {
            return "No change";
        }

        return "Something went wrong! try again";
    }

    public static WeatherHistorical getWeatherHistoricalData() {

        ObjectMapper objectMapper = new ObjectMapper();

        String responseApi = getResponse(WEATHERHISTORICAL);

        WeatherHistorical ws = null;
        try {
            ws = objectMapper.readValue(responseApi, WeatherHistorical.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return ws;

    }
}