package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
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
    @FXML private Label titleLabel;
    @FXML private Button replayButton;
    @FXML private ToggleButton knightBtn;
    @FXML private ToggleButton queenBtn;
    @FXML private ToggleButton pawnBtn;

    private ToggleGroup pieceGroup;

    private static final int SIZE = 8;
    private static final int CELL_SIZE = 80;
    private StackPane[][] cells = new StackPane[SIZE][SIZE];
    private int playerX, playerY, enemyX, enemyY, moves = 0;
    private int timeLeft = 120;
    private Timeline timer;
    private boolean playerTurn = true;

    private Image knightImage, queenImage, pawnImage, selectedImage, enemyImage;
    private String selectedPiece = "Knight", enemyType;

    @FXML
    public void initialize() {
        knightImage = new Image(getClass().getResource("/images/knight.png").toExternalForm());
        queenImage = new Image(getClass().getResource("/images/queen.png").toExternalForm());
        pawnImage = new Image(getClass().getResource("/images/pawn.png").toExternalForm());

        pieceGroup = new ToggleGroup();
        knightBtn.setToggleGroup(pieceGroup);
        queenBtn.setToggleGroup(pieceGroup);
        pawnBtn.setToggleGroup(pieceGroup);
        knightBtn.setSelected(true);

        pieceGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == knightBtn) {
                selectedPiece = "Knight";
                selectedImage = knightImage;
            } else if (newToggle == queenBtn) {
                selectedPiece = "Queen";
                selectedImage = queenImage;
            } else if (newToggle == pawnBtn) {
                selectedPiece = "Pawn";
                selectedImage = pawnImage;
            }
            titleLabel.setText("🧠 Brain Chess - Mode " + selectedPiece);
            startGame();
        });

        selectedImage = knightImage;
        setupBoard();
        startGame();
        replayButton.setOnAction(e -> startGame());
    }

    private void setupBoard() {
        board.getChildren().clear();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
                rect.setFill((i + j) % 2 == 0 ? Color.BEIGE : Color.BURLYWOOD);
                rect.setStroke(Color.DARKGRAY);

                StackPane cell = new StackPane(rect);
                cell.setAlignment(Pos.CENTER);
                int x = i, y = j;
                cell.setOnMouseClicked(e -> handleClick(x, y));

                cells[i][j] = cell;
                board.add(cell, y, i);
            }
        }
    }

    private void startGame() {
        moves = 0;
        timeLeft = 120;
        playerTurn = true;
        startTimer();
        clearPieces();

        Random rand = new Random();
        playerX = rand.nextInt(SIZE);
        playerY = rand.nextInt(SIZE);
        do {
            enemyX = rand.nextInt(SIZE);
            enemyY = rand.nextInt(SIZE);
        } while (enemyX == playerX && enemyY == playerY);

        enemyType = rand.nextBoolean() ? "Queen" : "Pawn";
        enemyImage = getImageForPiece(enemyType);
        selectedImage = getImageForPiece(selectedPiece);
        titleLabel.setText("🧠 Brain Chess - Mode " + selectedPiece);
        updateBoard();
        replayButton.setDisable(false);
    }

    private void startTimer() {
        if (timer != null) timer.stop();

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            timerLabel.setText("⏳ Temps : " + timeLeft + "s");
            if (timeLeft <= 0) {
                timer.stop();
                showAlert("⏰ Temps écoulé !", "Tu n'as pas capturé la " + enemyType + ".");
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
        if (!playerTurn) return;

        if (isValidMove(playerX, playerY, x, y, selectedPiece)) {
            playerX = x;
            playerY = y;
            moves++;

            if (playerX == enemyX && playerY == enemyY) {
                timer.stop();
                showAlert("🎉 Bravo !", "Tu as capturé la " + enemyType + " en " + moves + " coups !");
                replayButton.setDisable(true);
                return;
            }

            updateBoard();
            playerTurn = false;

            Timeline botDelay = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> botPlay()));
            botDelay.play();
        }
    }

    private void botPlay() {
        Random rand = new Random();
        int[][] directions = getDirectionsForPiece(enemyType);
        int maxSteps = getMaxStepsForPiece(enemyType);

        for (int attempt = 0; attempt < 100; attempt++) {
            int[] dir = directions[rand.nextInt(directions.length)];
            int step = rand.nextInt(maxSteps) + 1;
            int nx = enemyX + dir[0] * step;
            int ny = enemyY + dir[1] * step;

            if (isInBounds(nx, ny) && isValidMove(enemyX, enemyY, nx, ny, enemyType)) {
                enemyX = nx;
                enemyY = ny;

                if (enemyX == playerX && enemyY == playerY) {
                    timer.stop();
                    updateBoard();
                    showAlert("💀 Oops !", "Le bot t’a capturé !");
                    replayButton.setDisable(true);
                    return;
                }

                updateBoard();
                playerTurn = true;
                return;
            }
        }

        playerTurn = true;
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    private boolean isValidMove(int fromX, int fromY, int toX, int toY, String pieceType) {
        int dx = Math.abs(toX - fromX);
        int dy = Math.abs(toY - fromY);

        return switch (pieceType) {
            case "Knight" -> (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
            case "Queen" -> dx == dy || fromX == toX || fromY == toY;
            case "Pawn" -> toX == fromX + 1 && toY == fromY;
            default -> false;
        };
    }

    private int[][] getDirectionsForPiece(String pieceType) {
        return switch (pieceType) {
            case "Knight" -> new int[][]{{2,1},{1,2},{-1,2},{-2,1},{-2,-1},{-1,-2},{1,-2},{2,-1}};
            case "Queen" -> new int[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
            case "Pawn" -> new int[][]{{1,0}};
            default -> new int[0][0];
        };
    }

    private int getMaxStepsForPiece(String pieceType) {
        return switch (pieceType) {
            case "Knight", "Pawn" -> 1;
            case "Queen" -> 7;
            default -> 1;
        };
    }

    private Image getImageForPiece(String piece) {
        return switch (piece) {
            case "Knight" -> knightImage;
            case "Queen" -> queenImage;
            case "Pawn" -> pawnImage;
            default -> knightImage;
        };
    }

    private void updateBoard() {
        clearPieces();

        ImageView player = new ImageView(selectedImage);
        player.setFitWidth(50);
        player.setFitHeight(50);
        animatePiece(player);
        cells[playerX][playerY].getChildren().add(player);

        ImageView enemy = new ImageView(enemyImage);
        enemy.setFitWidth(50);
        enemy.setFitHeight(50);
        cells[enemyX][enemyY].getChildren().add(enemy);

        scoreLabel.setText("🏇 Coups : " + moves);
    }

    private void animatePiece(ImageView imageView) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), imageView);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void retour(javafx.event.ActionEvent event) {
        javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
