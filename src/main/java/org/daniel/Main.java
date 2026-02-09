package org.daniel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {

    static public final String GEOAPI = "https://geocoding-api.open-meteo.com/v1/search?name=";
    static public final String WEATHERAPI = "https://api.open-meteo.com/v1/forecast?";
    static public final String WEATHERHISTORICAL = "https://api.open-meteo.com/v1/forecast?latitude=54.3523&longitude=18.6491&hourly=temperature_2m&timezone=Europe%2FMoscow&past_days=2&forecast_days=1";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static HttpURLConnection connection;
    static String cityname = "Gdansk";

    public static void main(String[] args) throws InterruptedException {

        Location city;
        WeatherSnapshot weather;

        do {


            System.out.print("\n\nWhat do you want to do ? Choose from 1 to 5 and press Enter : \n"
                    + "1 - Get the temperature for hardcoded city which is " + cityname + "\n"
                    + "2 - Get the temperature for whatever city you like \n"
                    + "3 - Start collecting temperature for hardcoded city and calculate trend \n"
                    + "4 - Get historical data for hardcoded city and calculate trend \n"
                    + "5 - Exit \n\r"
            );


            int option = SCANNER.nextInt();
            SCANNER.nextLine();

            switch (option) {
                case 1:
                    city = getLocation(cityname);
                    weather = getWeather(city);
                    System.out.println("Temperature for city " + city.name + " is :" + weather.getCurrent().getTemperature2m() + weather.getCurrentUnits().getTemperature2m());
                    break;
                case 2:
                    System.out.print("Enter city name to get actual temperature:");
                    String newCity = SCANNER.nextLine();
                    System.out.println("You typed: " + newCity);
                    city = getLocation(newCity);
                    System.out.println(city.name + " longitude: " + city.longitude + " latitude: " + city.latitude + " country: " + city.country);
                    weather = getWeather(city);
                    System.out.println("Temperature for city " + city.name + " is :" + weather.getCurrent().getTemperature2m() + weather.getCurrentUnits().getTemperature2m());
                    break;
                case 3:
                    System.out.print("How long should I gather data in minutes?\n");
                    System.out.print("Warning! Everything less than 15 minutes doesn't make sense because API data refresh interval is 15 minutes\n");
                    int stop = SCANNER.nextInt();
                    SCANNER.nextLine();
                    Location finalCity = getLocation(cityname);

                    List<Double> data = new ArrayList<>();
                    System.out.println("Collecting data. Please wait !");
                    for (int a = 0; a < stop; a++) {

                        //System.out.println(a);
                        double temp = getWeather(finalCity).getCurrent().getTemperature2m();
                        data.add(temp);
                        //System.out.print(temp);
                        Thread.sleep(60000);

                    }

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
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(destination)).timeout(Duration.ofSeconds(30)).GET().build();
        try {
            return CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static Location getLocation(String cityName) {


        String response = getResponse(GEOAPI + cityName.replace(" ","+"));

        GeoResponse loc;
        List<Location> locList;
        try {
            loc = MAPPER.readValue(response, GeoResponse.class);

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


        String response = getResponse(WEATHERAPI + "latitude=" + desiredLocation.getLatitude() + "&longitude=" + desiredLocation.getLongitude() + "&current=temperature_2m");


        WeatherSnapshot ws = null;
        try {
            ws = MAPPER.readValue(response, WeatherSnapshot.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return ws;
    }

    public static String calculateTrend(List<Double> list) {

        int n = list.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < n; i++) {
            double y = list.get(i);
            sumX += i;
            sumY += y;
            sumXY += i * y;
            sumXX += i * i;
        }
        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        if (slope > 0) return "Getting warmer";
        if (slope < 0) return "Getting colder";
        return "No change";

    }

    public static WeatherHistorical getWeatherHistoricalData() {

        String responseApi = getResponse(WEATHERHISTORICAL);

        WeatherHistorical ws;
        try {
            ws = MAPPER.readValue(responseApi, WeatherHistorical.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return ws;

    }
}