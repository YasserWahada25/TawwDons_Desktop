package controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
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
    private final int CELL_SIZE = 60;
    private Rectangle[][] cells = new Rectangle[SIZE][SIZE];
    private int playerX = 0, playerY = 0;
    private int goalX, goalY;
    private int score = 0;
    private int timeLeft = 60;
    private Timeline timer;

    @FXML
    public void initialize() {
        setupBoard();
        startGame();
        replayButton.setOnAction(e -> startGame());
    }

    private void setupBoard() {
        board.getChildren().clear();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
                rect.setFill(Color.web("#2c2c2c"));
                rect.setStroke(Color.web("#3c3c3c"));
                int finalI = i, finalJ = j;
                rect.setOnMouseClicked(e -> handleClick(finalI, finalJ));
                cells[i][j] = rect;
                board.add(rect, j, i);
            }
        }
    }

    private void startGame() {
        playerX = new Random().nextInt(SIZE);
        playerY = new Random().nextInt(SIZE);
        do {
            goalX = new Random().nextInt(SIZE);
            goalY = new Random().nextInt(SIZE);
        } while (playerX == goalX && playerY == goalY);

        score = 0;
        timeLeft = 60;
        updateDisplay();
        startTimer();
    }

    private void startTimer() {
        if (timer != null) timer.stop();
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            timerLabel.setText("⏳ Temps : " + timeLeft + "s");
            if (timeLeft <= 0) {
                timer.stop();
                timerLabel.setText("⛔ Temps écoulé !");
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void handleClick(int x, int y) {
        // Déplacement autorisé : adjacent
        if (Math.abs(playerX - x) <= 1 && Math.abs(playerY - y) <= 1) {
            playerX = x;
            playerY = y;

            if (playerX == goalX && playerY == goalY) {
                score++;
                startGame(); // nouvelle position, objectif et timer reset
            } else {
                updateDisplay();
            }
        }
    }

    private void updateDisplay() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                cells[i][j].setFill(Color.web("#2c2c2c"));
            }
        }

        // Affiche joueur et but
        cells[playerX][playerY].setFill(Color.LIGHTBLUE); // joueur
        cells[goalX][goalY].setFill(Color.DARKGREEN);     // objectif

        scoreLabel.setText("🏆 Score : " + score);
        timerLabel.setText("⏳ Temps : " + timeLeft + "s");
    }
}
