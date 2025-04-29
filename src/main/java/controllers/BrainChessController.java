package controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Random;

public class BrainChessController {

    @FXML private GridPane board;
    @FXML private Label scoreLabel;
    @FXML private Label timerLabel;
    @FXML private Button replayButton;

    private final int SIZE = 8;
    private final int CELL_SIZE = 80;
    private StackPane[][] cells = new StackPane[SIZE][SIZE];
    private int playerX, playerY;
    private int enemyX, enemyY;
    private int moves = 0;
    private Timeline timer;
    private int timeLeft = 120;

    private Image knightImage;
    private Image queenImage;
    private Image pawnImage;
    private Image pawnOrQueenImage;

    private String enemyType;

    @FXML
    public void initialize() {
        knightImage = new Image(getClass().getResource("/images/knight.png").toExternalForm());
        queenImage = new Image(getClass().getResource("/images/queen.png").toExternalForm());
        pawnImage = new Image(getClass().getResource("/images/pawn.png").toExternalForm());

        setupBoard();
        startGame();

        replayButton.setOnAction(e -> startGame());
    }

    private void setupBoard() {
        board.getChildren().clear();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
                rect.setFill((i + j) % 2 == 0 ? Color.BEIGE : Color.BROWN);
                rect.setStroke(Color.BLACK);

                StackPane cell = new StackPane(rect);
                int finalI = i, finalJ = j;
                cell.setOnMouseClicked(e -> handleClick(finalI, finalJ));

                cells[i][j] = cell;
                board.add(cell, j, i);
            }
        }
    }

    private void startGame() {
        moves = 0;
        timeLeft = 120;
        startTimer();

        clearPieces();

        playerX = new Random().nextInt(SIZE);
        playerY = new Random().nextInt(SIZE);

        do {
            enemyX = new Random().nextInt(SIZE);
            enemyY = new Random().nextInt(SIZE);
        } while (playerX == enemyX && playerY == enemyY);

        enemyType = new Random().nextBoolean() ? "Queen" : "Pawn";
        pawnOrQueenImage = enemyType.equals("Queen") ? queenImage : pawnImage;

        updateBoard();
    }

    private void startTimer() {
        if (timer != null) timer.stop();

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            timerLabel.setText("⏳ Temps : " + timeLeft + "s");
            if (timeLeft <= 0) {
                timer.stop();
                showAlert("⏰ Temps écoulé !", "Tu n'as pas capturé la " + enemyType + "...");
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void clearPieces() {
        for (StackPane[] row : cells) {
            for (StackPane cell : row) {
                if (cell.getChildren().size() > 1) {
                    cell.getChildren().remove(1);
                }
            }
        }
    }

    private void handleClick(int x, int y) {
        if (isValidKnightMove(x, y)) {
            playerX = x;
            playerY = y;
            moves++;

            if (playerX == enemyX && playerY == enemyY) {
                timer.stop();
                showAlert("🎉 Bravo !", "Tu as capturé la " + enemyType + " en " + moves + " coups!");
            }

            updateBoard();
        }
    }

    private boolean isValidKnightMove(int x, int y) {
        int dx = Math.abs(x - playerX);
        int dy = Math.abs(y - playerY);
        return (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
    }

    private void updateBoard() {
        clearPieces();

        ImageView knight = new ImageView(knightImage);
        knight.setFitHeight(50);
        knight.setFitWidth(50);
        cells[playerX][playerY].getChildren().add(knight);

        ImageView enemy = new ImageView(pawnOrQueenImage);
        enemy.setFitHeight(50);
        enemy.setFitWidth(50);
        cells[enemyX][enemyY].getChildren().add(enemy);

        scoreLabel.setText("🏇 Coups : " + moves);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
        startGame();
    }

    @FXML
    private void retour(javafx.event.ActionEvent event) {
        javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close(); // Fermer la fenêtre actuelle
    }

}
