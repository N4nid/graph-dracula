import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;


public class CustomVarUIElement {
    public String name;
    public double value;
    private int sliderDecimalPlaces = 1;
    private TwoDVec<Double> sliderRange = new TwoDVec<>(-5.0,5.0);
    public double currentWidth = 268;

    public Pane background;
    private Pane labelBox;
    private Label nameDisplay;
    private Slider valueSlider;
    private TextField valueInput;
    private TextField minValDisplay;
    private TextField maxValDisplay;
    private ApplicationController controller;

    public static final int defaultHeight = 73;
    private static final int labelWidth = 40;
    private static final int labelBoxPadding = 30;
    private static final double defaultSliderHeightPadding = 10;
    private static final double rangeDisplayPadding = 3;
    private static final double rangeDisplayWidth = 50;
    private static final TwoDVec<Double> labelPos = new TwoDVec<Double>(25.0,0.0);

    public CustomVarUIElement(String name,ApplicationController controller) {
        this.name = name;
        this.controller = controller;
        value = 1;

        background = new Pane();
        nameDisplay = new Label();
        valueSlider = new Slider();
        valueInput = new TextField();

        background.getStyleClass().add("black");
        background.getStyleClass().add("border");

        labelBox = new Pane();
        labelBox.getStyleClass().add("black");
        labelBox.getStyleClass().add("border");
        background.getChildren().add(labelBox);

        nameDisplay.getStyleClass().add("normal-text");
        nameDisplay.setText(name);
        labelBox.getChildren().add(nameDisplay);

        valueInput = new TextField();
        valueInput.getStyleClass().add("black");
        valueInput.getStyleClass().add("border");
        valueInput.getStyleClass().add("small-text");
        valueInput.setPadding(new Insets(0,8,0,8));
        background.getChildren().add(valueInput);

        valueSlider = new Slider(sliderRange.x,sliderRange.y,0);
        background.getChildren().add(valueSlider);
        valueInput.setText("" + value);

        minValDisplay = new TextField();
        setupRangeDisplay(minValDisplay,false);
        background.getChildren().add(minValDisplay);
        maxValDisplay = new TextField();
        setupRangeDisplay(maxValDisplay,true);
        background.getChildren().add(maxValDisplay);

        refresheUI();

        valueSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            value = round(valueSlider.getValue(),sliderDecimalPlaces);
            valueSlider.setValue(value);
            valueInput.setText("" + value);
            controller.updateRenderCanvas();
        });

        valueInput.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!valueInput.isFocused()) {
                parseSliderValueInput();
            }
        });

        valueInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                background.requestFocus();
            }
        });
    }

    private void setupRangeDisplay(TextField display, boolean isMax) {
        double xPos = (isMax)? (currentWidth - rangeDisplayWidth - rangeDisplayPadding) : rangeDisplayPadding;
        double displayVal = (isMax)? sliderRange.y : sliderRange.x;
        display.setPrefWidth(rangeDisplayWidth);
        display.setPrefHeight(defaultHeight/2-3);
        display.relocate(xPos,defaultHeight/2);
        display.getStyleClass().add("smallest-text");
        display.getStyleClass().add("black");
        display.setPadding(new Insets(0,5,0,5));
        display.setAlignment(Pos.CENTER);
        display.setText("" + displayVal);

        display.focusedProperty().addListener((obs, oldVal, newVal) -> {
            double newRangeValue = (isMax)? sliderRange.y : sliderRange.x;
            try {
                newRangeValue = Double.parseDouble(display.getText());
            } catch (Exception e) {
                System.out.println("Error: Invalid number inputted (at: rangeInput of parameter)");
            }
            if (isMax) {
                if (newRangeValue <= sliderRange.x) {
                    System.out.println("Error: Max value of slider range must be bigger than min value!");
                    newRangeValue = sliderRange.y;
                }
                setSliderRange(new TwoDVec<Double>(valueSlider.getMin(),newRangeValue));
            }
            else {
                if (newRangeValue >= sliderRange.y) {
                    System.out.println("Error: Min value of slider range must be smaller than max value!");
                    newRangeValue = sliderRange.x;
                }
                setSliderRange(new TwoDVec<Double>(newRangeValue,valueSlider.getMax()));
            }
        });

        display.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                background.requestFocus();
            }
        });
    }

    private int countDecimalPlaces(double value) {
        String valueString = "" + value;
        String[] valueParts = valueString.split("\\.");
        if (valueParts.length > 1) {
            if (valueParts[1].equals("0")) {
                return 0;
            }
            return valueParts[1].length();
        }
        return 0;
    }

    private void parseSliderValueInput() {
        double newValue = value;
        try {
            newValue = Double.parseDouble(valueInput.getText());
        } catch (Exception e){
            System.out.println("Error: Invalid number inputted (at: valueInput of parameter)");
            return;
        }
        value = newValue;
        if (value > valueSlider.getMax()) {
            setSliderRange(new TwoDVec<Double>(valueSlider.getMin(),value));
        }
        if (value < valueSlider.getMin()) {
            setSliderRange(new TwoDVec<Double>(value,valueSlider.getMax()));
        }
        if (countDecimalPlaces(value) > sliderDecimalPlaces) {
            sliderDecimalPlaces = countDecimalPlaces(value);
        }
        valueSlider.setValue(value);
        valueInput.setText("" + value);
        controller.updateRenderCanvas();
    }

    private void setSliderRange(TwoDVec<Double> range) {
        sliderRange.setPos(range.x,range.y);
        valueSlider.setMin(sliderRange.x);
        valueSlider.setMax(sliderRange.y);
        minValDisplay.setText("" + sliderRange.x);
        maxValDisplay.setText("" + sliderRange.y);
        sliderDecimalPlaces = Math.max(countDecimalPlaces(sliderRange.x),countDecimalPlaces(sliderRange.y)) + 1;
    }

    public void refresheUI() {
        background.setPrefHeight(defaultHeight);
        background.setPrefWidth(currentWidth);
        nameDisplay.relocate(labelPos.x,labelPos.y);
        valueSlider.setLayoutX(rangeDisplayWidth + rangeDisplayPadding);
        valueSlider.setLayoutY(defaultSliderHeightPadding + defaultHeight/2);
        valueSlider.setPrefWidth(currentWidth - (rangeDisplayPadding + rangeDisplayWidth) * 2);
        valueInput.setLayoutX(labelWidth+labelBoxPadding-3);
        labelBox.setPrefHeight(defaultHeight/2);
        labelBox.setPrefWidth(labelWidth + labelBoxPadding);
        nameDisplay.setPrefWidth(labelWidth);
        valueInput.setPrefHeight(defaultHeight/2);
        valueInput.setPrefWidth(currentWidth-labelWidth-labelBoxPadding+3);
        setupRangeDisplay(minValDisplay,false);
        setupRangeDisplay(maxValDisplay,true);
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
