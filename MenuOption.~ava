import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;


public class MenuOption {
    public Pane optionPane;
    private Label optionText;
    private ImageView icon;

    private static final double textOffset = 10;
    private static final int iconSize = 25;
    private static final double xOffset = 7;

    public MenuOption(String optionText, Image icon, MenuHaver menuHaver, TwoDVec<Double> dimensions, TwoDVec<Double> position, Pane parent) {
        setup(optionText,icon,iconSize,-1,menuHaver,dimensions,position,parent);
    }

    public MenuOption(String optionText, Image icon, int iconSize, int fontSize, MenuHaver menuHaver, TwoDVec<Double> dimensions, TwoDVec<Double> position, Pane parent) {
        setup(optionText,icon,iconSize,fontSize,menuHaver,dimensions,position,parent);
    }

    private void setup(String optionText, Image icon, int iconSize, int fontSize, MenuHaver menuHaver, TwoDVec<Double> dimensions, TwoDVec<Double> position, Pane parent) {
        double yOffset = (dimensions.y - iconSize) / 2;

        optionPane = new Pane();
        optionPane.setLayoutX(position.x);
        optionPane.setLayoutY(position.y);
        optionPane.setPrefWidth(dimensions.x);
        optionPane.setPrefHeight(dimensions.y);
        optionPane.getStyleClass().add("black");
        optionPane.getStyleClass().add("border");
        Effects.addDefaultHoverEffect(optionPane);

        this.optionText = new Label();
        this.optionText.setText(optionText);
        this.optionText.setPrefWidth(dimensions.x - textOffset);
        this.optionText.setLayoutX(iconSize + textOffset + xOffset);
        this.optionText.setLayoutY(yOffset - 5);
        this.optionText.setText(optionText);
        this.optionText.getStyleClass().add("normal-text");
        this.optionText.setStyle("-fx-text-fill: white");
        if (fontSize != -1) {
            this.optionText.setStyle("-fx-font-size: " + fontSize + "px");
        }
        optionPane.getChildren().add(this.optionText);

        this.icon = new ImageView();
        this.icon.setImage(icon);
        this.icon.setLayoutX(xOffset);
        this.icon.setLayoutY(yOffset);
        this.icon.setFitWidth(iconSize);
        this.icon.setFitHeight(iconSize);
        optionPane.getChildren().add(this.icon);

        this.optionPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                menuHaver.executeMenuOption(optionText);
            }
        });

        parent.getChildren().add(optionPane);
    }
}
