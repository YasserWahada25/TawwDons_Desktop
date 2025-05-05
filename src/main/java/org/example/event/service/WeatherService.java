package org.example.event.service;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class WeatherService {

    private static final String API_KEY = "50625e93cef1823127ddfc96c23b22d9";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public WeatherData getWeatherData(String coordinates, LocalDate date) {
        try {
            String[] parts = coordinates.split(",");
            if (parts.length != 2) {
                return new WeatherData("Format invalide: utilisez 'latitude,longitude'");
            }

            double lat = Double.parseDouble(parts[0].trim());
            double lon = Double.parseDouble(parts[1].trim());

            return getWeatherData(lat, lon, date);

        } catch (NumberFormatException e) {
            return new WeatherData("Coordonnées numériques invalides");
        } catch (Exception e) {
            return new WeatherData("Erreur de connexion au service météo");
        }
    }

    public WeatherData getWeatherData(double lat, double lon, LocalDate date) {
        final String targetDate = date.format(API_DATE_FORMAT);

        try {
            String apiUrl = String.format(
                    "%s?lat=%.6f&lon=%.6f&appid=%s&units=metric",
                    BASE_URL, lat, lon, API_KEY
            );

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                return new WeatherData("Service météo indisponible");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(response.toString());
            JSONArray forecasts = json.getJSONArray("list");

            for (int i = 0; i < forecasts.length(); i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                String dtText = forecast.getString("dt_txt");

                if (dtText.startsWith(targetDate)) {
                    JSONObject main = forecast.getJSONObject("main");
                    JSONArray weather = forecast.getJSONArray("weather");

                    return new WeatherData(
                            main.getDouble("temp"),
                            weather.getJSONObject(0).getString("main"),
                            weather.getJSONObject(0).getString("description")
                    );
                }
            }

            return new WeatherData("Aucune prévision pour cette date");

        } catch (Exception e) {
            return new WeatherData("Erreur de traitement des données");
        }
    }

    public static class WeatherData {
        public final double temperature;
        public final String condition;
        public final String description;
        public final String error;

        public WeatherData(double temp, String cond, String desc) {
            this.temperature = temp;
            this.condition = cond;
            this.description = desc;
            this.error = null;
        }

        public WeatherData(String error) {
            this.temperature = -274; // Valeur impossible (absolu zéro)
            this.condition = "N/A";
            this.description = "N/A";
            this.error = error;
        }
    }
}