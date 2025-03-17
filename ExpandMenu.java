import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;


public class ExpandMenu {
    public Pane background;
    private double xMargin = 60;
    private double height = 200;
    private TwoDVec<Double> standardButtonSize = new TwoDVec<Double>(99.875,52.0);
    public ExpandMenu(Pane root) {
        background = new Pane();
        background.getStyleClass().add("black");
        background.getStyleClass().add("border");
        background.relocate(xMargin,0);
        background.setPrefHeight(height);
        background.setViewOrder(-1);
        root.getChildren().add(background);
    }
}

class MathButton {
    private ExpandMenu parentMenu;

    private String inputString;
    private int cursorPos;

    private Button baseButton;

    private MathButton(ExpandMenu parentMenu, String displayImagePath, String inputString, int cursorPos, TwoDVec<Double> pos, TwoDVec<Double> scale) {
        baseButton = new Button();
        baseButton.getStyleClass().add("black");
        baseButton.getStyleClass().add("border");
        baseButton.getStyleClass().add("image-button");
        baseButton.setStyle("-fx-background-image: url('" + displayImagePath + "');");
    }
}
