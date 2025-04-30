package models;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ChessTile extends StackPane {
    private int x, y;
    private boolean isBrain = false;
    private boolean isGoal = false;
    private Rectangle border;
    private Label emojiLabel;

    public ChessTile(int x, int y) {
        this.x = x;
        this.y = y;

        border = new Rectangle(70, 70);
        border.setFill((x + y) % 2 == 0 ? Color.web("#1e1e1e") : Color.web("#2c2c2c"));
        border.setStroke(Color.web("#3d3d3d"));

        emojiLabel = new Label();
        emojiLabel.setStyle("-fx-font-size: 24px;");

        getChildren().addAll(border, emojiLabel);
    }

    public void setBrain(boolean value) {
        isBrain = value;
        updateEmoji();
    }

    public void setGoal(boolean value) {
        isGoal = value;
        updateEmoji();
    }

    private void updateEmoji() {
        if (isBrain) {
            emojiLabel.setText("🧠");
        } else if (isGoal) {
            emojiLabel.setText("🏁");
        } else {
            emojiLabel.setText("");
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isGoal() { return isGoal; }
}
