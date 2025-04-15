package controllers.condidat;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import models.Condidat;
import models.Offre;
import services.CondidatService;
import services.OffreService;
import utils.Router;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class UpdateCondidatController implements Initializable {

    @FXML
    private ComboBox<Offre> offreComboBox;
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextField cvField;
    @FXML
    private Button browseButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button cancelButton;
    @FXML
    private MenuItem menuListeCondidats;
    @FXML
    private MenuItem menuAjouterCondidat;
    @FXML
    private Button offreBtn;
    @FXML
    private Button condidatBtn;

    private CondidatService condidatService;
    private OffreService offreService;
    private Condidat condidatToUpdate;
    private File selectedCvFile;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 50;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            condidatService = new CondidatService();
            offreService = new OffreService();

            setupNavigation();
            loadOffres();
            setupValidation();
            loadCondidatData();
        } catch (Exception e) {
            showError("Erreur d'initialisation", "Une erreur est survenue lors de l'initialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupNavigation() {
        menuListeCondidats.setOnAction(e -> Router.navigateTo("/condidat/ListCondidat_BC.fxml"));
        menuAjouterCondidat.setOnAction(e -> Router.navigateTo("/condidat/AddCondidat.fxml"));
        offreBtn.setOnAction(e -> Router.navigateTo("/offre/ListOffre_BC.fxml"));
        condidatBtn.setOnAction(e -> Router.navigateTo("/condidat/ListCondidat_BC.fxml"));
    }

    private void loadOffres() {
        try {
            offreComboBox.getItems().addAll(offreService.getAllOffres());
            offreComboBox.setCellFactory(lv -> new ListCell<Offre>() {
                @Override
                protected void updateItem(Offre item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getTitreOffre());
                }
            });
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les offres : " + e.getMessage());
        }
    }

    private void setupValidation() {
        // Validation de l'email
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                if (EMAIL_PATTERN.matcher(newVal).matches()) {
                    emailField.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 2px;");
                } else {
                    emailField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                    showError("Format d'email invalide", "Veuillez entrer une adresse email valide.");
                }
            } else {
                emailField.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1px;");
            }
        });

        // Validation du téléphone
        telephoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                if (PHONE_PATTERN.matcher(newVal).matches()) {
                    telephoneField.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 2px;");
                } else {
                    telephoneField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                    showError("Format de téléphone invalide", "Le numéro de téléphone doit contenir exactement 8 chiffres.");
                }
            } else {
                telephoneField.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1px;");
            }
        });

        // Validation du nom
        nomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                if (newVal.length() >= MIN_NAME_LENGTH && newVal.length() <= MAX_NAME_LENGTH) {
                    nomField.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 2px;");
                } else {
                    nomField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                    showError("Longueur de nom invalide", "Le nom doit contenir entre " + MIN_NAME_LENGTH + " et " + MAX_NAME_LENGTH + " caractères.");
                }
            } else {
                nomField.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1px;");
            }
        });

        // Validation du prénom
        prenomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                if (newVal.length() >= MIN_NAME_LENGTH && newVal.length() <= MAX_NAME_LENGTH) {
                    prenomField.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 2px;");
                } else {
                    prenomField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                    showError("Longueur de prénom invalide", "Le prénom doit contenir entre " + MIN_NAME_LENGTH + " et " + MAX_NAME_LENGTH + " caractères.");
                }
            } else {
                prenomField.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1px;");
            }
        });
    }

    private void loadCondidatData() {
        try {
            String url = Router.getCurrentUrl();
            System.out.println("URL actuelle : " + url); // Log pour débogage

            if (url != null && url.contains("id=")) {
                String idParam = url.substring(url.indexOf("id=") + 3);
                int condidatId = Integer.parseInt(idParam);
                System.out.println("ID du candidat : " + condidatId); // Log pour débogage

                condidatToUpdate = condidatService.getCondidatById(condidatId);
                if (condidatToUpdate != null) {
                    System.out.println("Candidat trouvé : " + condidatToUpdate); // Log pour débogage
                    populateFields();
                } else {
                    showError("Candidat non trouvé", "Le candidat avec l'ID " + condidatId + " n'existe pas.");
                    Router.navigateTo("/condidat/ListCondidat_BC.fxml");
                }
            } else {
                showError("ID manquant", "Aucun ID de candidat n'a été fourni dans l'URL.");
                Router.navigateTo("/condidat/ListCondidat_BC.fxml");
            }
        } catch (NumberFormatException e) {
            showError("Format d'ID invalide", "L'ID du candidat n'est pas un nombre valide.");
            Router.navigateTo("/condidat/ListCondidat_BC.fxml");
        } catch (Exception e) {
            showError("Erreur de chargement", "Une erreur est survenue lors du chargement des données : " + e.getMessage());
            e.printStackTrace();
            Router.navigateTo("/condidat/ListCondidat_BC.fxml");
        }
    }

    public void setCondidatToUpdate(Condidat condidat) {
        this.condidatToUpdate = condidat;
        populateFields();
    }

    private void populateFields() {
        try {
            if (condidatToUpdate != null) {
                nomField.setText(condidatToUpdate.getNom());
                prenomField.setText(condidatToUpdate.getPrenom());
                emailField.setText(condidatToUpdate.getEmail());
                telephoneField.setText(String.valueOf(condidatToUpdate.getTelephone()));
                cvField.setText(condidatToUpdate.getCv());

                // Sélectionner l'offre correspondante
                for (Offre offre : offreComboBox.getItems()) {
                    if (offre.getId() == condidatToUpdate.getOffreId()) {
                        offreComboBox.setValue(offre);
                        break;
                    }
                }
                System.out.println("Champs remplis avec succès"); // Log pour débogage
            }
        } catch (Exception e) {
            showError("Erreur", "Erreur lors du remplissage des champs : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBrowseButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un CV");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("Word Documents", "*.doc", "*.docx")
        );

        selectedCvFile = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
        if (selectedCvFile != null) {
            cvField.setText(selectedCvFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleUpdateButtonClick() {
        try {
            if (validateFields()) {
                condidatToUpdate.setNom(nomField.getText().trim());
                condidatToUpdate.setPrenom(prenomField.getText().trim());
                condidatToUpdate.setEmail(emailField.getText().trim());
                condidatToUpdate.setTelephone(Integer.parseInt(telephoneField.getText().trim()));
                
                if (offreComboBox.getValue() != null) {
                    condidatToUpdate.setOffreId(offreComboBox.getValue().getId());
                }
                
                if (selectedCvFile != null) {
                    condidatToUpdate.setCv(selectedCvFile.getAbsolutePath());
                }

                boolean success = condidatService.updateCondidat(condidatToUpdate);
                if (success) {
                    showSuccess("Succès", "Le candidat a été mis à jour avec succès.");
                    Router.navigateTo("/condidat/ListCondidat_BC.fxml");
                } else {
                    showError("Erreur", "La mise à jour du candidat a échoué.");
                }
            }
        } catch (Exception e) {
            showError("Erreur", "Une erreur est survenue lors de la mise à jour : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (nomField.getText().isEmpty() || nomField.getText().length() < MIN_NAME_LENGTH || nomField.getText().length() > MAX_NAME_LENGTH) {
            errors.append("Le nom doit contenir entre ").append(MIN_NAME_LENGTH).append(" et ").append(MAX_NAME_LENGTH).append(" caractères.\n");
        }

        if (prenomField.getText().isEmpty() || prenomField.getText().length() < MIN_NAME_LENGTH || prenomField.getText().length() > MAX_NAME_LENGTH) {
            errors.append("Le prénom doit contenir entre ").append(MIN_NAME_LENGTH).append(" et ").append(MAX_NAME_LENGTH).append(" caractères.\n");
        }

        if (emailField.getText().isEmpty() || !EMAIL_PATTERN.matcher(emailField.getText()).matches()) {
            errors.append("L'email n'est pas valide.\n");
        }

        if (telephoneField.getText().isEmpty() || !PHONE_PATTERN.matcher(telephoneField.getText()).matches()) {
            errors.append("Le numéro de téléphone doit contenir exactement 8 chiffres.\n");
        }

        if (offreComboBox.getValue() == null) {
            errors.append("Veuillez sélectionner une offre.\n");
        }

        if (errors.length() > 0) {
            showError("Erreur de validation", errors.toString());
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancelButtonClick() {
        Router.navigateTo("/condidat/ListCondidat_BC.fxml");
    }

    @FXML
    private void handleOffreButtonClick() {
        Router.navigateTo("/offre/ListOffre_BC.fxml");
    }

    @FXML
    private void handleCondidatButtonClick() {
        Router.navigateTo("/condidat/ListCondidat_BC.fxml");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 