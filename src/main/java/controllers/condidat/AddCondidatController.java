package controllers.condidat;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import models.Condidat;
import models.Offre;
import services.CondidatService;
import services.OffreService;
import utils.Router;

import java.io.File;
import java.util.List;

public class AddCondidatController {

    @FXML private ComboBox<Offre> offreComboBox;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField cvField;
    @FXML private Button browseButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private MenuItem menuListeCondidats;
    @FXML private MenuItem menuAjouterCondidat;
    @FXML private Button offreBtn;
    @FXML private Button condidatBtn;

    private final CondidatService condidatService = new CondidatService();
    private final OffreService offreService = new OffreService();
    private File selectedCvFile;

    @FXML
    public void initialize() {
        try {
            // Configuration de la navigation
            setupNavigation();
            
            // Charger les offres dans la ComboBox
            loadOffres();
            
            // Configuration des validations
            setupValidations();
            
            // Configuration des styles initiaux
            setupInitialStyles();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation", 
                     "Une erreur est survenue lors de l'initialisation : " + e.getMessage());
        }
    }

    private void setupInitialStyles() {
        // Style pour les champs obligatoires
        String requiredStyle = "-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 3;";
        nomField.setStyle(requiredStyle);
        prenomField.setStyle(requiredStyle);
        emailField.setStyle(requiredStyle);
        telephoneField.setStyle(requiredStyle);
        cvField.setStyle(requiredStyle);
        offreComboBox.setStyle(requiredStyle);
    }

    private void setupNavigation() {
        menuListeCondidats.setOnAction(e -> Router.navigateTo("/condidat/ListCondidat_BC.fxml"));
        menuAjouterCondidat.setOnAction(e -> Router.navigateTo("/condidat/AddCondidat.fxml"));
        cancelButton.setOnAction(e -> Router.navigateTo("/condidat/ListCondidat_BC.fxml"));
    }
    
    private void loadOffres() {
        try {
            List<Offre> offres = offreService.getAllOffres();
            if (offres.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", 
                         "Aucune offre n'est disponible. Veuillez d'abord créer une offre.");
            }
            
            offreComboBox.getItems().addAll(offres);
            
            // Configurer l'affichage des offres dans la ComboBox
            offreComboBox.setCellFactory(lv -> new ListCell<Offre>() {
                @Override
                protected void updateItem(Offre item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText("");
                    } else {
                        setText(item.getTitreOffre());
                    }
                }
            });

            // Ajouter un listener pour le changement de sélection
            offreComboBox.setOnAction(e -> {
                Offre selectedOffre = offreComboBox.getValue();
                if (selectedOffre != null) {
                    offreComboBox.setStyle("-fx-border-color: #2ecc71;");
                } else {
                    offreComboBox.setStyle("-fx-border-color: #e74c3c;");
                }
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Impossible de charger les offres : " + e.getMessage());
        }
    }
    
    private void setupValidations() {
        // Validation du téléphone (exactement 8 chiffres)
        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Ne garder que les chiffres
            String digitsOnly = newValue.replaceAll("[^\\d]", "");
            
            // Limiter à 8 chiffres
            if (digitsOnly.length() > 8) {
                digitsOnly = digitsOnly.substring(0, 8);
            }
            
            // Mettre à jour le champ si nécessaire
            if (!digitsOnly.equals(newValue)) {
                telephoneField.setText(digitsOnly);
            }
            
            // Mettre à jour le style en fonction de la validité
            updateFieldStyle(telephoneField, digitsOnly.length() == 8);
        });
        
        // Validation de l'email (format *@*.*)
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = newValue.isEmpty() || 
                            newValue.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
            updateFieldStyle(emailField, isValid);
        });

        // Validation du nom et prénom (pas de caractères spéciaux)
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
            // Validation des champs
            if (!validateFields()) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", 
                         "Veuillez remplir tous les champs obligatoires correctement.");
                return;
            }

            // Création du candidat
            Condidat condidat = new Condidat();
            Offre selectedOffre = offreComboBox.getValue();
            
            if (selectedOffre != null) {
                condidat.setOffreId(selectedOffre.getId());
            }
            
            condidat.setNom(nomField.getText().trim());
            condidat.setPrenom(prenomField.getText().trim());
            condidat.setEmail(emailField.getText().trim());
            condidat.setTelephone(Integer.parseInt(telephoneField.getText().trim()));
            condidat.setCv(selectedCvFile != null ? selectedCvFile.getAbsolutePath() : null);

            // Sauvegarde du candidat
            if (condidatService.ajouterCondidat(condidat)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                         "Le candidat a été ajouté avec succès.");
                Router.navigateTo("/condidat/ListCondidat_BC.fxml");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                         "Une erreur est survenue lors de l'ajout du candidat.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Une erreur est survenue : " + e.getMessage());
        }
    }

    private boolean validateFields() {
        boolean isValid = true;
        
        // Validation de l'offre
        if (offreComboBox.getValue() == null) {
            offreComboBox.setStyle("-fx-border-color: #e74c3c;");
            isValid = false;
        }
        
        // Validation du nom
        if (nomField.getText().trim().isEmpty() || nomField.getText().trim().length() < 2) {
            nomField.setStyle("-fx-border-color: #e74c3c;");
            isValid = false;
        }
        
        // Validation du prénom
        if (prenomField.getText().trim().isEmpty() || prenomField.getText().trim().length() < 2) {
            prenomField.setStyle("-fx-border-color: #e74c3c;");
            isValid = false;
        }
        
        // Validation de l'email (format *@*.*)
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            emailField.setStyle("-fx-border-color: #e74c3c;");
            isValid = false;
        }
        
        // Validation du téléphone (exactement 8 chiffres)
        String phone = telephoneField.getText().trim();
        if (phone.isEmpty() || phone.length() != 8 || !phone.matches("\\d{8}")) {
            telephoneField.setStyle("-fx-border-color: #e74c3c;");
            isValid = false;
        }
        
        // Validation du CV
        if (selectedCvFile == null) {
            cvField.setStyle("-fx-border-color: #e74c3c;");
            isValid = false;
        }
        
        return isValid;
    }

    @FXML
    private void handleOffreButtonClick() {
        Router.navigateTo("/offre/ListOffre_BC.fxml");
    }
    
    @FXML
    private void handleCondidatButtonClick() {
        Router.navigateTo("/condidat/ListCondidat_BC.fxml");
    }

    @FXML
    private void handleCancelButtonClick() {
        Router.navigateTo("/condidat/ListCondidat_BC.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 