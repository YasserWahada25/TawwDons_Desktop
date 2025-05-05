package controllers.condidat;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import models.Condidat;
import models.Offre;
import services.CondidatService;
import services.OffreService;
import utils.Router;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AddCondidat_fr_Controller {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField cvField;
    @FXML private Button browseButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button btnHome;
    @FXML private MenuItem menuListeDons;
    @FXML private MenuItem menuPosterDon;
    @FXML private MenuItem menuListeOffres;
    @FXML private MenuItem menuPosterOffre;

    private final CondidatService condidatService = new CondidatService();
    private final OffreService offreService = new OffreService();
    private File selectedCvFile;
    private int offreId;
    private String offreProfession;
    private boolean isProfessionCompatible = false;

    @FXML
    public void initialize() {
        try {
            // Configuration de la navigation
            setupNavigation();
            
            // Configuration des validations
            setupValidations();
            
            // Configuration des styles initiaux
            setupInitialStyles();

            // Récupérer l'ID de l'offre depuis l'URL
            String url = Router.getCurrentUrl();
            if (url != null && url.contains("id=")) {
                String idStr = url.substring(url.indexOf("id=") + 3);
                try {
                    offreId = Integer.parseInt(idStr);
                    // Récupérer la profession de l'offre
                    Offre offre = offreService.getOffreById(offreId);
                    if (offre != null) {
                        offreProfession = offre.getTitreOffre();
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "ID d'offre invalide");
                    Router.navigateTo("/offre/ListOffres.fxml");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune offre sélectionnée");
                Router.navigateTo("/offre/ListOffres.fxml");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation", 
                     "Une erreur est survenue lors de l'initialisation : " + e.getMessage());
        }
    }

    private void setupInitialStyles() {
        String requiredStyle = "-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 3;";
        nomField.setStyle(requiredStyle);
        prenomField.setStyle(requiredStyle);
        emailField.setStyle(requiredStyle);
        telephoneField.setStyle(requiredStyle);
        cvField.setStyle(requiredStyle);
        saveButton.setDisable(true);
    }

    private void setupNavigation() {
        btnHome.setOnAction(e -> Router.navigateTo("/Home.fxml"));
        menuListeDons.setOnAction(e -> Router.navigateTo("/ListDons.fxml"));
        menuPosterDon.setOnAction(e -> Router.navigateTo("/AddDons.fxml"));
        menuListeOffres.setOnAction(e -> Router.navigateTo("/offre/ListOffres.fxml"));
        menuPosterOffre.setOnAction(e -> Router.navigateTo("/offre/AddOffre.fxml"));
        cancelButton.setOnAction(e -> Router.navigateTo("/offre/ListOffres.fxml"));
    }
    
    private void setupValidations() {
        // Validation du téléphone (exactement 8 chiffres)
        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            String digitsOnly = newValue.replaceAll("[^\\d]", "");
            if (digitsOnly.length() > 8) {
                digitsOnly = digitsOnly.substring(0, 8);
            }
            if (!digitsOnly.equals(newValue)) {
                telephoneField.setText(digitsOnly);
            }
            updateFieldStyle(telephoneField, digitsOnly.length() == 8);
        });
        
        // Validation de l'email
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = newValue.isEmpty() || 
                            newValue.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
            updateFieldStyle(emailField, isValid);
        });

        // Validation du nom et prénom
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-ZÀ-ÿ\\s]*")) {
                nomField.setText(oldValue);
            }
            updateFieldStyle(nomField, !newValue.isEmpty());
        });

        prenomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-ZÀ-ÿ\\s]*")) {
                prenomField.setText(oldValue);
            }
            updateFieldStyle(prenomField, !newValue.isEmpty());
        });
    }

    private void updateFieldStyle(TextField field, boolean isValid) {
        if (isValid) {
            field.setStyle("-fx-border-color: #2ecc71;");
        } else {
            field.setStyle("-fx-border-color: #e74c3c;");
        }
    }

    @FXML
    private void handleBrowseButtonClick() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner un CV");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Documents", "*.doc", "*.docx")
            );
            
            selectedCvFile = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
            if (selectedCvFile != null) {
                cvField.setText(selectedCvFile.getAbsolutePath());
                cvField.setStyle("-fx-border-color: #2ecc71;");
                analyzeCV(selectedCvFile);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors de la sélection du fichier : " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveButtonClick() {
        try {
            if (!isProfessionCompatible) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                         "Vous ne pouvez pas postuler car votre CV ne correspond pas à l'offre.");
                return;
            }

            if (!validateFields()) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", 
                         "Veuillez remplir tous les champs obligatoires correctement.");
                return;
            }

            Condidat condidat = new Condidat();
            condidat.setOffreId(offreId);
            condidat.setNom(nomField.getText().trim());
            condidat.setPrenom(prenomField.getText().trim());
            condidat.setEmail(emailField.getText().trim());
            condidat.setTelephone(Integer.parseInt(telephoneField.getText().trim()));
            condidat.setCv(selectedCvFile != null ? selectedCvFile.getAbsolutePath() : null);

            if (condidatService.ajouterCondidat(condidat)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                         "Votre candidature a été envoyée avec succès.");
                Router.navigateTo("/offre/ListOffres.fxml");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                         "Une erreur est survenue lors de l'envoi de votre candidature.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Une erreur est survenue : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelButtonClick() {
        Router.navigateTo("/offre/ListOffres.fxml");
    }

    private boolean validateFields() {
        boolean isValid = true;
        
        if (nomField.getText().trim().isEmpty() || nomField.getText().trim().length() < 2) {
            nomField.setStyle("-fx-border-color: #e74c3c;");
            isValid = false;
        }
        
        if (prenomField.getText().trim().isEmpty() || prenomField.getText().trim().length() < 2) {
            prenomField.setStyle("-fx-border-color: #e74c3c;");
            isValid = false;
        }
        
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            emailField.setStyle("-fx-border-color: #e74c3c;");
            isValid = false;
        }
        
        String phone = telephoneField.getText().trim();
        if (phone.isEmpty() || phone.length() != 8 || !phone.matches("\\d{8}")) {
            telephoneField.setStyle("-fx-border-color: #e74c3c;");
            isValid = false;
        }
        
        if (selectedCvFile == null) {
            cvField.setStyle("-fx-border-color: #e74c3c;");
            isValid = false;
        }
        
        return isValid;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void analyzeCV(File cvFile) {
        try {
            // Extraire le texte du PDF
            String cvText = extractTextFromPdf(cvFile);
            
            // Analyser le contenu avec Gemini
            String prompt = "Analyse ce CV et extrait la profession principale mentionnée. Réponds uniquement avec la profession, sans phrases supplémentaires.\n\n" + cvText;
            
            new Thread(() -> {
                String cvProfession = callGeminiAPI(prompt);
                javafx.application.Platform.runLater(() -> {
                    if (offreProfession != null) {
                        compareProfessions(cvProfession, offreProfession);
                    }
                });
            }).start();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'analyse du CV : " + e.getMessage());
        }
    }

    private String extractTextFromPdf(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String callGeminiAPI(String prompt) {
        String apiKey = "AIzaSyCGvIXkpsIwFejR_3h9W_aqz20WFaVqwzc";
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-latest:generateContent?key=" + apiKey;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        JSONObject message = new JSONObject()
                .put("role", "user")
                .put("parts", new JSONArray().put(new JSONObject().put("text", prompt)));

        JSONObject json = new JSONObject()
                .put("contents", new JSONArray().put(message));

        RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toString());

        Request request = new Request.Builder()
                .url(endpoint)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "Erreur: " + response.code() + " - " + response.message();
            }

            String responseBody = response.body().string();
            JSONObject result = new JSONObject(responseBody);

            if (!result.has("candidates") || result.getJSONArray("candidates").length() == 0) {
                return "Erreur: Format de réponse inattendu";
            }

            return result
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

        } catch (Exception e) {
            return "Erreur API: " + e.getMessage();
        }
    }

    private void compareProfessions(String cvProfession, String offerProfession) {
        String prompt = "Compare ces deux professions et dis si elles sont compatibles ou similaires. " +
                       "Profession du CV : " + cvProfession + "\n" +
                       "Profession de l'offre : " + offerProfession + "\n" +
                       "Réponds uniquement avec 'Compatible' ou 'Non compatible' suivi d'une brève explication.";

        new Thread(() -> {
            String comparison = callGeminiAPI(prompt);
            javafx.application.Platform.runLater(() -> {
                if (comparison.toLowerCase().contains("non compatible")) {
                    isProfessionCompatible = false;
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Incompatibilité de profession");
                    alert.setHeaderText("Postulation impossible");
                    alert.setContentText(comparison + "\n\nVeuillez sélectionner un CV correspondant à l'offre.");
                    alert.getButtonTypes().setAll(ButtonType.OK);
                    
                    alert.showAndWait();
                    cvField.clear();
                    selectedCvFile = null;
                    saveButton.setDisable(true);
                } else {
                    isProfessionCompatible = true;
                    saveButton.setDisable(false);
                }
            });
        }).start();
    }
} 