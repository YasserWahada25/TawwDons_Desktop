package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class CommentAnalyzer {
    private static final String API_URL    = "https://api.sightengine.com/1.0/text/check.json";
    private final String apiUser;
    private final String apiSecret;
    private final HttpClient http;

    public CommentAnalyzer(String apiUser, String apiSecret) {
        this.apiUser   = apiUser;
        this.apiSecret = apiSecret;
        this.http      = HttpClient.newHttpClient();
    }


    public JsonNode analyze(String text) throws IOException, InterruptedException {
        var form = Map.<String, String>of(
                "text",       text,
                "lang",       "en",
                "categories", "profanity,personal,link,drug,weapon,spam,extremism,violence,self-harm,medical",
                "mode",       "standard",
                "api_user",   apiUser,
                "api_secret", apiSecret
        );

        String body = form.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(),   StandardCharsets.UTF_8)
                        + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return new ObjectMapper().readTree(resp.body());
    }


    public boolean shouldReject(JsonNode analysis) {
        for (JsonNode m : analysis.path("profanity").path("matches")) {
            String intensity = m.path("intensity").asText("");
            if ("high".equals(intensity) || "medium".equals(intensity)) {
                return true;
            }
        }
        for (String cat : new String[]{
                "personal","link","drug","weapon",
                "spam","extremism","violence",
                "self-harm","medical"
        }) {
            if (analysis.path(cat).path("matches").size() > 0) {
                return true;
            }
        }
        return false;
    }
}
