import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;


public class CustomVarUIElement {
    public String name;
    public double value;

    private Pane background;
    private Pane labelBox;
    private Pane valueBox;
    private Label nameDisplay;
    private Slider valueSlider;
    private TextField valueInput;

    private static final int defaultWidth = 268;
    private static final int defaultHeight = 73;
    private static final int labelWidth = 40;
    private static final int labelBoxPadding = 30;
    private static final double defaultSliderWidthPadding = 20;
    private static final double defaultSliderHeightPadding = 10;
    private static final TwoDVec<Double> labelPos = new TwoDVec<Double>(25.0,0.0);
    private static final TwoDVec<Double> sliderRange = new TwoDVec<>(-5.0,5.0);

    public CustomVarUIElement(String name, Pane parent, double yPos) {
        this.name = name;
        value = 0;

        background = new Pane();
        nameDisplay = new Label();
        valueSlider = new Slider();
        valueInput = new TextField();

        background.setViewOrder(-10);
        background.getStyleClass().add("black");
        background.getStyleClass().add("border");
        background.setPrefHeight(defaultHeight);
        background.setPrefWidth(defaultWidth);

        labelBox = new Pane();
        labelBox.setPrefHeight(defaultHeight/2);
        labelBox.setPrefWidth(labelWidth + labelBoxPadding);
        labelBox.getStyleClass().add("black");
        labelBox.getStyleClass().add("border");
        background.getChildren().add(labelBox);

        nameDisplay.getStyleClass().add("normal-text");
        nameDisplay.relocate(labelPos.x,labelPos.y);
        nameDisplay.setPrefWidth(labelWidth);
        nameDisplay.setText(name);
        labelBox.getChildren().add(nameDisplay);

        valueInput = new TextField();
        valueInput.getStyleClass().add("black");
        valueInput.getStyleClass().add("border");
        valueInput.getStyleClass().add("small-text");
        valueInput.setPrefHeight(defaultHeight/2);
        valueInput.setPrefWidth(defaultWidth-labelWidth-labelBoxPadding+3);
        valueInput.setLayoutX(labelWidth+labelBoxPadding-3);
        valueInput.setPadding(new Insets(0,8,0,8));
        background.getChildren().add(valueInput);

        valueSlider = new Slider(sliderRange.x,sliderRange.y,0);
        valueSlider.setPrefWidth(defaultWidth - defaultSliderWidthPadding*2);
        valueSlider.setLayoutX(defaultSliderWidthPadding);
        valueSlider.setLayoutY(defaultSliderHeightPadding + defaultHeight/2);
        //valueSlider.setStyle("-fx-control-inner-background: ");
        //valueSlider.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY,new Insets(0))));
        background.getChildren().add(valueSlider);

        parent.getChildren().add(background);
    }
}
