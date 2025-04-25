package services;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Service for handling Google authentication using OAuth2 with system browser
 * and automatic code extraction via a temporary local HTTP server.
 */
public class GoogleAuthService {
    // OAuth2 parameters loaded from config file
    private String clientId;
    private String clientSecret;
    
    // Configuration - we'll find an available port dynamically
    private static final int[] PORTS_TO_TRY = {8765, 8080, 8081, 8082, 8083, 8084, 8085, 3000, 9000};
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    
    private HttpServer server;
    private Stage loadingStage;
    private int serverPort;
    private String redirectUri;
    
    /**
     * Constructor that loads OAuth credentials from config file
     */
    public GoogleAuthService() {
        loadCredentials();
    }
    
    /**
     * Loads OAuth credentials from config file
     */
    private void loadCredentials() {
        try {
            // Look for credentials file in multiple locations
            Path configPath = Paths.get("config", "oauth_credentials.properties");
            if (!Files.exists(configPath)) {
                configPath = Paths.get("oauth_credentials.properties");
            }
            
            // Create default config file if it doesn't exist
            if (!Files.exists(configPath)) {
                createDefaultConfigFile(configPath);
                showConfigFileCreatedAlert(configPath);
                // Default values for development only - should be replaced in production
                clientId = "REPLACE_WITH_YOUR_CLIENT_ID";
                clientSecret = "REPLACE_WITH_YOUR_CLIENT_SECRET";
                return;
            }
            
            // Load properties from file
            Properties props = new Properties();
            try (InputStream input = new FileInputStream(configPath.toFile())) {
                props.load(input);
            }
            
            // Get credentials
            clientId = props.getProperty("google.client.id");
            clientSecret = props.getProperty("google.client.secret");
            
            // Trim any whitespace that might have been accidentally added
            if (clientId != null) clientId = clientId.trim();
            if (clientSecret != null) clientSecret = clientSecret.trim();
            
            System.out.println("Loaded credentials from: " + configPath.toAbsolutePath());
            System.out.println("Client ID length: " + (clientId != null ? clientId.length() : "null"));
            System.out.println("Client Secret length: " + (clientSecret != null ? clientSecret.length() : "null"));
            
            // Validate credentials
            if (clientId == null || clientId.isEmpty() || 
                clientId.equals("REPLACE_WITH_YOUR_CLIENT_ID") ||
                clientSecret == null || clientSecret.isEmpty() || 
                clientSecret.equals("REPLACE_WITH_YOUR_CLIENT_SECRET")) {
                
                showInvalidCredentialsAlert(configPath);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            clientId = "REPLACE_WITH_YOUR_CLIENT_ID";
            clientSecret = "REPLACE_WITH_YOUR_CLIENT_SECRET";
            showCredentialsLoadingErrorAlert(e.getMessage());
        }
    }
    
    /**
     * Creates default config file with placeholder values
     */
    private void createDefaultConfigFile(Path configPath) throws IOException {
        // Create directory if it doesn't exist
        if (!Files.exists(configPath.getParent())) {
            Files.createDirectories(configPath.getParent());
        }
        
        // Write default config file
        String defaultConfig = 
            "# Google OAuth credentials\n" +
            "# Replace these values with your actual Google Cloud Console credentials\n" +
            "google.client.id=REPLACE_WITH_YOUR_CLIENT_ID\n" +
            "google.client.secret=REPLACE_WITH_YOUR_CLIENT_SECRET\n";
        
        Files.write(configPath, defaultConfig.getBytes());
    }
    
    /**
     * Shows alert when config file is created
     */
    private void showConfigFileCreatedAlert(Path configPath) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("OAuth Configuration Required");
            alert.setHeaderText("OAuth Credentials Not Found");
            alert.setContentText("A default configuration file has been created at:\n" + 
                configPath.toAbsolutePath() + "\n\n" +
                "Please edit this file to add your Google OAuth credentials from Google Cloud Console.");
            alert.showAndWait();
        });
    }
    
    /**
     * Shows alert when credentials are invalid
     */
    private void showInvalidCredentialsAlert(Path configPath) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid OAuth Configuration");
            alert.setHeaderText("OAuth Credentials Not Configured");
            alert.setContentText("Please edit the configuration file at:\n" + 
                configPath.toAbsolutePath() + "\n\n" +
                "Add your Google OAuth credentials from Google Cloud Console.");
            alert.showAndWait();
        });
    }
    
    /**
     * Shows alert when credentials loading fails
     */
    private void showCredentialsLoadingErrorAlert(String errorMessage) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("OAuth Configuration Error");
            alert.setHeaderText("Failed to Load OAuth Credentials");
            alert.setContentText("Error: " + errorMessage);
            alert.showAndWait();
        });
    }

    /**
     * Initiates the Google sign-in process using the system's default browser
     * and automatically captures the callback using a temporary HTTP server.
     */
    public CompletableFuture<Map<String, String>> signInWithGoogle() {
        CompletableFuture<Map<String, String>> future = new CompletableFuture<>();
        
        try {
            // Verify credentials are set
            if (clientId == null || clientId.isEmpty() || 
                clientId.equals("REPLACE_WITH_YOUR_CLIENT_ID") ||
                clientSecret == null || clientSecret.isEmpty() || 
                clientSecret.equals("REPLACE_WITH_YOUR_CLIENT_SECRET")) {
                
                throw new Exception("OAuth credentials not configured. Please set your Google OAuth credentials in the configuration file.");
            }
            
            // Show loading indicator
            showLoadingDialog("Connexion avec Google en cours...");
            
            // Find an available port and configure the redirect URI
            if (!findAvailablePortAndConfigureRedirectUri()) {
                closeLoadingDialog();
                throw new Exception("Impossible de trouver un port disponible. Veuillez redémarrer votre ordinateur et réessayer.");
            }
            
            // Start the server on the port we found
            if (!startLocalServer(future)) {
                closeLoadingDialog();
                throw new Exception("Impossible de démarrer le serveur local sur le port " + serverPort + ".");
            }
            
            // Build and open the authorization URL
            String authUrl = buildAuthorizationUrl();
            System.out.println("Opening Google Auth URL in browser: " + authUrl);
            
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else {
                closeLoadingDialog();
                stopServer();
                throw new Exception("Unable to open browser. System doesn't support desktop browsing.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            closeLoadingDialog();
            stopServer();
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Shows a loading dialog during the authentication process.
     */
    private void showLoadingDialog(String message) {
        Platform.runLater(() -> {
            loadingStage = new Stage();
            loadingStage.initModality(Modality.APPLICATION_MODAL);
            loadingStage.setTitle("Google Authentication");
            
            ProgressIndicator progress = new ProgressIndicator();
            progress.setProgress(-1); // Indeterminate progress
            
            javafx.scene.control.Label label = new javafx.scene.control.Label(message);
            label.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
            
            StackPane root = new StackPane();
            root.getChildren().addAll(progress, label);
            label.setTranslateY(50);
            
            Scene scene = new Scene(root, 300, 150);
            loadingStage.setScene(scene);
            loadingStage.show();
        });
    }
    
    /**
     * Closes the loading dialog.
     */
    private void closeLoadingDialog() {
        Platform.runLater(() -> {
            if (loadingStage != null) {
                loadingStage.close();
                loadingStage = null;
            }
        });
    }
    
    /**
     * Finds an available port and configures the redirect URI accordingly
     * @return true if a port was found, false otherwise
     */
    private boolean findAvailablePortAndConfigureRedirectUri() {
        // Try each port in sequence
        for (int port : PORTS_TO_TRY) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                // If we get here, the port is available
                serverPort = port;
                redirectUri = "http://localhost:" + serverPort + "/callback";
                System.out.println("Found available port: " + serverPort);
                System.out.println("Using redirect URI: " + redirectUri);
                return true;
            } catch (IOException e) {
                // This port is in use, try the next one
                System.out.println("Port " + port + " is already in use, trying next port");
            }
        }
        
        // If we get here, all ports are in use
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("All Ports In Use");
            alert.setHeaderText("No available ports found");
            alert.setContentText("All ports between " + PORTS_TO_TRY[0] + " and " + PORTS_TO_TRY[PORTS_TO_TRY.length - 1] + 
                " are in use. Please close other applications or restart your computer to free up ports.");
            alert.showAndWait();
        });
        
        return false;
    }
    
    /**
     * Starts a local HTTP server to listen for the OAuth callback.
     */
    private boolean startLocalServer(CompletableFuture<Map<String, String>> future) {
        try {
            server = HttpServer.create(new InetSocketAddress(serverPort), 0);
            
            server.createContext("/callback", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String query = exchange.getRequestURI().getQuery();
                    String response = "<html><head><title>Authentication Successful</title>"
                        + "<style>body{font-family:Arial,sans-serif;text-align:center;padding-top:50px;background-color:#f5f5f5;}"
                        + "h2{color:#4285f4;} p{margin:20px;}</style></head>"
                        + "<body>"
                        + "<h2>Authentication Successful!</h2>"
                        + "<p>You can now close this window and return to the application.</p>"
                        + "<script>setTimeout(function() { window.close(); }, 3000);</script>"
                        + "</body></html>";
                    
                    exchange.getResponseHeaders().add("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, response.length());
                    
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    
                    // Extract the authorization code from the query
                    if (query != null && query.contains("code=")) {
                        Pattern pattern = Pattern.compile("code=([^&]+)");
                        Matcher matcher = pattern.matcher(query);
                        if (matcher.find()) {
                            String authCode = matcher.group(1);
                            System.out.println("Received authorization code: " + authCode);
                            
                            // Process the authorization code
                            exchangeCodeForTokens(authCode, future);
                        } else {
                            closeLoadingDialog();
                            future.completeExceptionally(new RuntimeException("No authorization code found in callback"));
                        }
                    } else if (query != null && query.contains("error=")) {
                        closeLoadingDialog();
                        Pattern pattern = Pattern.compile("error=([^&]+)");
                        Matcher matcher = pattern.matcher(query);
                        if (matcher.find()) {
                            String error = matcher.group(1);
                            future.completeExceptionally(new RuntimeException("Authentication error: " + error));
                        } else {
                            future.completeExceptionally(new RuntimeException("Unknown authentication error"));
                        }
                    }
                    
                    // Stop the server after handling the request
                    stopServer();
                }
            });
            
            server.setExecutor(null);
            server.start();
            System.out.println("Local server started on port " + serverPort);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to start server on port " + serverPort + ": " + e.getMessage());
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Server Error");
                alert.setHeaderText("Could not start local authorization server");
                alert.setContentText("The application could not start a local server on port " + serverPort + 
                    ". This port may have been taken by another application after we checked its availability. " +
                    "Please try again or restart your computer.");
                alert.showAndWait();
            });
            return false;
        }
    }
    
    /**
     * Stops the temporary HTTP server.
     */
    private void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
            System.out.println("Local server stopped");
        }
    }
    
    /**
     * Builds the OAuth2 authorization URL with the detected port.
     */
    private String buildAuthorizationUrl() {
        try {
            String scope = URLEncoder.encode("email profile", StandardCharsets.UTF_8.toString());
            
            System.out.println("Using redirect URI for auth: " + redirectUri);
            
            return AUTH_URL + 
                   "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8.toString()) +
                   "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.toString()) +
                   "&scope=" + scope +
                   "&response_type=code" +
                   "&access_type=offline" +
                   "&prompt=consent";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
    /**
     * Exchanges the authorization code for access and refresh tokens.
     */
    private void exchangeCodeForTokens(String authCode, CompletableFuture<Map<String, String>> future) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                HttpURLConnection conn = null;
                try {
                    // Debug logging
                    System.out.println("=== OAuth Debug Information ===");
                    System.out.println("Auth Code: " + authCode);
                    System.out.println("Client ID: " + clientId);
                    System.out.println("Client Secret length: " + (clientSecret != null ? clientSecret.length() : "null"));
                    System.out.println("Redirect URI: " + redirectUri);
                    
                    // Prepare the token request
                    URL url = new URL(TOKEN_URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoOutput(true);
                    
                    // Prepare the request body with proper URL encoding
                    StringBuilder requestBodyBuilder = new StringBuilder();
                    appendParam(requestBodyBuilder, "code", authCode, true);
                    appendParam(requestBodyBuilder, "client_id", clientId, false);
                    appendParam(requestBodyBuilder, "client_secret", clientSecret, false);
                    appendParam(requestBodyBuilder, "redirect_uri", redirectUri, false);
                    appendParam(requestBodyBuilder, "grant_type", "authorization_code", false);
                    
                    String requestBody = requestBodyBuilder.toString();
                    System.out.println("Token request body: " + requestBody);
                    
                    // Send the request
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                        os.flush();
                    }
                    
                    // Check for successful response
                    int responseCode = conn.getResponseCode();
                    System.out.println("Token response code: " + responseCode);
                    
                    if (responseCode == 200) {
                        // Parse the response
                        StringBuilder response = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                        }
                        
                        String responseBody = response.toString();
                        System.out.println("Token response: " + responseBody);
                        
                        // Parse the tokens
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String accessToken = jsonResponse.getString("access_token");
                        
                        // Now get the user info using the access token
                        getUserInfo(accessToken, future);
                    } else {
                        StringBuilder errorResponse = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                errorResponse.append(line);
                            }
                        }
                        
                        String errorBody = errorResponse.toString();
                        System.err.println("Error response: " + errorBody);
                        
                        // Show detailed error information
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Google Authentication Error");
                            alert.setHeaderText("OAuth Error: " + responseCode);
                            
                            String message = "Authentication failed with response code: " + responseCode + "\n\n";
                            message += "Error details: " + errorBody + "\n\n";
                            
                            if (errorBody.contains("invalid_client")) {
                                message += "This is likely due to incorrect OAuth credentials.\n" +
                                    "Please check your Client ID and Client Secret in the config file.";
                            } else if (errorBody.contains("redirect_uri_mismatch")) {
                                message += "The redirect URI used (" + redirectUri + ") " +
                                    "does not match what's configured in Google Cloud Console.\n\n" +
                                    "Please add this exact URI to the authorized redirect URIs in your Google Cloud Console.";
                            }
                            
                            alert.setContentText(message);
                            alert.showAndWait();
                        });
                        
                        closeLoadingDialog();
                        future.completeExceptionally(new RuntimeException("Error exchanging code for tokens: " + responseCode + " - " + errorBody));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    closeLoadingDialog();
                    future.completeExceptionally(e);
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
                return null;
            }
        };
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Helper method to append a URL-encoded parameter to the request body.
     */
    private void appendParam(StringBuilder builder, String name, String value, boolean isFirst) throws IOException {
        if (!isFirst) {
            builder.append("&");
        }
        builder.append(URLEncoder.encode(name, StandardCharsets.UTF_8.toString()));
        builder.append("=");
        builder.append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()));
    }
    
    /**
     * Gets the user's information using the access token.
     */
    private void getUserInfo(String accessToken, CompletableFuture<Map<String, String>> future) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(USER_INFO_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            
            int responseCode = conn.getResponseCode();
            System.out.println("User info response code: " + responseCode);
            
            if (responseCode == 200) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
                
                String responseBody = response.toString();
                System.out.println("User info response: " + responseBody);
                
                JSONObject userInfo = new JSONObject(responseBody);
                String email = userInfo.getString("email");
                String name = userInfo.getString("name");
                String givenName = userInfo.getString("given_name");
                String familyName = userInfo.getString("family_name");
                String picture = userInfo.optString("picture", "");
                String googleId = userInfo.getString("sub");
                
                Map<String, String> userData = new HashMap<>();
                userData.put("email", email);
                userData.put("name", name);
                userData.put("givenName", givenName);
                userData.put("familyName", familyName);
                userData.put("picture", picture);
                userData.put("googleId", googleId);
                
                closeLoadingDialog();
                future.complete(userData);
            } else {
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                }
                
                String errorBody = errorResponse.toString();
                System.err.println("Error getting user info: " + errorBody);
                closeLoadingDialog();
                future.completeExceptionally(new RuntimeException("Error getting user info: " + responseCode + " - " + errorBody));
            }
        } catch (IOException e) {
            e.printStackTrace();
            closeLoadingDialog();
            future.completeExceptionally(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
} 