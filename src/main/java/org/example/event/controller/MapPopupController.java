package org.example.event.controller;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.util.function.Consumer;

public class MapPopupController {

    private double selectedLat = 0.0;
    private double selectedLng = 0.0;
    private Stage dialogStage;
    private Consumer<String> addressConsumer;

    @FXML private WebView webView;

    private static final String MAP_HTML = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>OpenStreetMap</title>
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css"/>
            <style>
                #map { height: 100%; width: 100%; }
                html, body { margin: 0; padding: 0; height: 100%; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
            <script>
                var map;
                var marker;
                function initMap() {
                    map = L.map('map').setView([36.8065, 10.1815], 14);
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '&copy; OpenStreetMap contributors'
                    }).addTo(map);
                    marker = L.marker(map.getCenter(), {draggable: true}).addTo(map);
                    map.on('click', function(e) {
                        marker.setLatLng(e.latlng);
                        window.javaBridge.updatePosition(e.latlng.lat, e.latlng.lng);
                    });
                    marker.on('dragend', function(e) {
                        var pos = marker.getLatLng();
                        window.javaBridge.updatePosition(pos.lat, pos.lng);
                    });
                }
                window.onload = initMap;
            </script>
        </body>
        </html>
        """;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setAddressConsumer(Consumer<String> consumer) {
        this.addressConsumer = consumer;
    }

    @FXML
    public void initialize() {
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);
        engine.loadContent(MAP_HTML);

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaBridge", new JavaBridge());
            }
        });
    }

    public class JavaBridge {
        public void updatePosition(double lat, double lng) {
            selectedLat = lat;
            selectedLng = lng;

            // Appel à l'API de géocodage inverse
            new Thread(() -> {
                try {
                    String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + lat + "&lon=" + lng;
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .header("User-Agent", "JavaFXApp")  // requis par Nominatim
                            .build();
                    Response response = client.newCall(request).execute();
                    String jsonStr = response.body().string();
                    JSONObject json = new JSONObject(jsonStr);

                    String address = json.optString("display_name", lat + "," + lng);

                    Platform.runLater(() -> {
                        if (addressConsumer != null) {
                            addressConsumer.accept(address);
                        }
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        if (addressConsumer != null) {
                            addressConsumer.accept(lat + "," + lng);
                        }
                    });
                }
            }).start();
        }

        public void sendAddress(String address) {
            Platform.runLater(() -> {
                if (addressConsumer != null) {
                    addressConsumer.accept(address);
                }
                dialogStage.close();
            });
        }
    }

    @FXML
    private void handleValidate() {
        // Plus nécessaire avec la logique actuelle
        dialogStage.close();
    }
}
