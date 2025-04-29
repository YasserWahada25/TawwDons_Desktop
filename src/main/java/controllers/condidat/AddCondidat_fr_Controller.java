package controllers.condidat;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import models.Condidat;
import services.CondidatService;
import utils.Router;

import java.io.File;

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
    private File selectedCvFile;
    private int offreId;

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
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors de la sélection du fichier : " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveButtonClick() {
        try {
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
} 