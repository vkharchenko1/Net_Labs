package ru.nsu.ccfit.network.g20202.kharchenko.lab3.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.nsu.ccfit.network.g20202.kharchenko.lab3.model.response.Place;
import ru.nsu.ccfit.network.g20202.kharchenko.lab3.model.response.Sight;
import ru.nsu.ccfit.network.g20202.kharchenko.lab3.model.response.Weather;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class PlaceFinder {

    final String GET_GEOCODE_ADDRESS = "https://graphhopper.com/api/1/geocode";
    final String GET_WEATHER_ADDRESS = "http://api.openweathermap.org/data/2.5/weather";
    final String GET_SIGHTS_ADDRESS = "http://api.opentripmap.com/0.1/en/places/radius";
    final String GET_DESC_ADDRESS = "https://api.opentripmap.com/0.1/ru/places/xid/";

    final String KEY_RESOURCE = "key.properties";

    String geocode_key;
    String openweather_key;
    String opentripmap_key;

    HttpClient httpClient = HttpClient.newHttpClient();

    // Extract the api keys
    public PlaceFinder() throws IOException {
        // Open properties
        Properties properties = new Properties();
        properties.load(Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(KEY_RESOURCE)
        );

        //Get keys from properties
        geocode_key = properties.getProperty("geocode_key");
        openweather_key = properties.getProperty("openweather_key");
        opentripmap_key = properties.getProperty("opentripmap_key");
    }

    // Get a list of places
    public CompletableFuture<List<Place>> requestPlaces(String placeName) {
        String requestURI = String.format("%s?q=%s&locale=en&key=%s",
                GET_GEOCODE_ADDRESS,
                placeName.replaceAll(" ", "-"),
                geocode_key
            );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestURI))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(HttpResponse::body)
                .thenApplyAsync(PlaceFinder::readPlaceList);
    }

    // Parse list of places
    static List<Place> readPlaceList(String jsonString) {
        List<Place> placeList = new ArrayList<>();

        JsonObject list = JsonParser.parseString(jsonString).getAsJsonObject();
        JsonArray hits = list.getAsJsonArray("hits");

        String name, lat, lng;
        for (int i = 0; i < hits.size(); i++) {
            name = hits.get(i).getAsJsonObject().get("name").getAsString();

            if (name.isBlank())
                continue;

            lat = hits.get(i).getAsJsonObject().getAsJsonObject("point").get("lat").getAsString();
            lng  = hits.get(i).getAsJsonObject().getAsJsonObject("point").get("lng").getAsString();

            placeList.add(new Place(name, lat, lng));
        }

        return placeList;
    }


    // Get weather
    public CompletableFuture<Weather> requestWeather(String lat, String lng) {
        String requestURI = String.format("%s?lat=%s&lon=%s&appid=%s",
                GET_WEATHER_ADDRESS,
                lat,
                lng,
                openweather_key
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestURI))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(PlaceFinder::readWeather);
    }

    // Read weather
    private static Weather readWeather(String jsonString) {
        JsonObject w = JsonParser.parseString(jsonString).getAsJsonObject();

        JsonArray jsonWeather = w.getAsJsonArray("weather");
        String main = jsonWeather.get(0).getAsJsonObject().get("main").getAsString();
        String desc = jsonWeather.get(0).getAsJsonObject().get("description").getAsString();

        return new Weather(main, desc);
    }

    public CompletableFuture<List<Sight>> requestSights(String lat, String lng) {
        String radius = "1000";
        String requestURI = String.format("%s?lang=en&radius=%s&lat=%s&lon=%s&apikey=%s",
                GET_SIGHTS_ADDRESS,
                radius,
                lat,
                lng,
                opentripmap_key
        );

        System.out.println(requestURI);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestURI))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(HttpResponse::body)
                .thenApplyAsync(PlaceFinder::readSights);
    }

    // Get a list of sights
    private static List<Sight> readSights(String jsonString) {
        List<Sight> sightsList = new ArrayList<>();

        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
        JsonArray features = json.getAsJsonArray("features");

        String xid, name;
        for (int i = 0; i < features.size(); i++) {
            JsonObject properties = features.get(i).getAsJsonObject().get("properties").getAsJsonObject();

            xid = properties.get("xid").getAsString();
            name = properties.get("name").getAsString();

            if (name.isBlank())
                continue;

            sightsList.add(new Sight(xid, name));
        }

        return sightsList;
    }

    // Get description of a sight
    public CompletionStage<String> requestSightDescription(String xid) {
        String requestURI = String.format("%s%s?lang=ru&apikey=%s",
                GET_DESC_ADDRESS,
                xid,
                opentripmap_key
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestURI))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(PlaceFinder::readDesc);
    }

    private static String readDesc(String jsonString) {
        String desc;

        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
        if (json.has("info")) {
            JsonObject info = json.get("info").getAsJsonObject();
            desc = info.get("descr").getAsString();
        }
        else {
            desc = json.get("kinds").getAsString();
        }

        return desc;
    }

}