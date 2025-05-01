import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.MyDataBase;
import utils.Router;

import java.sql.Connection;

public class Main extends Application {

    public static void main(String[] args) {

        Connection connection = MyDataBase.getInstance().getConnection();
        System.out.println("connecté à la base de données");
        System.out.println(connection);
        new Thread(() -> websocket.WebSocketLauncher.main(null)).start(); // Lance le WebSocket dans un thread
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1 Fenêtre pour le bénéficiaire
            FXMLLoader loaderBeneficiaire = new FXMLLoader(getClass().getResource("/Dons/ListDons.fxml"));
            Parent rootBeneficiaire = loaderBeneficiaire.load();
            Stage stageBeneficiaire = new Stage();
            stageBeneficiaire.setTitle("🧍 Bénéficiaire");
            stageBeneficiaire.setScene(new Scene(rootBeneficiaire));
            stageBeneficiaire.show();

//            // 2️ Fenêtre pour le donneur
//            FXMLLoader loaderDonneur = new FXMLLoader(getClass().getResource("/ListDemandePourDonneur.fxml"));
//            Parent rootDonneur = loaderDonneur.load();
//            Stage stageDonneur = new Stage();
//            stageDonneur.setTitle("🎁 Donneur");
//            stageDonneur.setScene(new Scene(rootDonneur));
//            stageDonneur.show();

//            // 3 Fenêtre pour le Admin
//            FXMLLoader loaderAdmin = new FXMLLoader(getClass().getResource("/Admin/RequestAddDons.fxml"));
//            Parent rootAdmin = loaderAdmin.load();
//            Stage stageAdmin  = new Stage();
//            stageDonneur.setTitle("🎁 Admin ");
//            stageDonneur.setScene(new Scene(rootAdmin));
//            stageAdmin.show();



            // Facultatif : enregistrer le mainStage si tu veux utiliser Router
            Router.setMainStage(stageBeneficiaire);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}













//// src/main/java/Main.java
//import javafx.application.Application;
//import javafx.scene.Scene;
//import javafx.scene.Parent;
//import javafx.stage.Stage;
//import utils.MyDataBase;
//import utils.Router;
//
//import java.sql.Connection;
//
//public class Main extends Application {
//
//    public static void main(String[] args) {
//        Connection connection = MyDataBase.getInstance().getConnection();
//        System.out.println("connecté à la base de données");
//        System.out.println(connection);
//        launch(args);
//    }
//
//    @Override
//    public void start(Stage primaryStage) {
//        Router.setMainStage(primaryStage);
//
//
//        // 2. Charger la première page via le Router
//      //Router.navigateTo("/ListDemandePourBeneficiaire.fxml");
//       //  Router.navigateTo("/Admin/RequestAddDons.fxml"); // Chemin relatif dans resources
//         Router.navigateTo("/ListDemandePourDonneur.fxml"); // Chemin relatif dans resources
//        // Router.navigateTo("/chat.fxml");
//
//
//
//        primaryStage.setTitle("TawwaDon App");
//        primaryStage.show();
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//    }
//}
