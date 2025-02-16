import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;

public class EquationVisElement implements MenuHaver{
    //Javafx-Elements
    public Pane pane;
    public Label funcDisplay;
    public RoundColorPicker colorPicker;

    //Data
    public EquationTree equation;
    public String equationText;
    private ApplicationController controller;

    //Default-Values
    private static final int defaultXPos = 23;
    private static final int defaultWidth = 358;
    private static final int defaultHeight = 73;
    private static final int labelWidth = 290;
    private static final int labelX = 69;
    private static final int labelY = 0;
    private static final int colorX = 13;
    private static final int colorY = 14;

    public EquationVisElement(EquationTree equation, String equationText, Pane parent, Pane root, ScrollPane scrollpane, int yPos, ApplicationController controller, int defaultColor) {
        this.controller = controller;
        this.equation = equation;
        this.equationText = equationText;

        pane = new Pane();
        pane.setLayoutX(defaultXPos);
        pane.setLayoutY(yPos);
        pane.setPrefWidth(defaultWidth);
        pane.setPrefHeight(defaultHeight);
        pane.getStyleClass().add("black");
        pane.getStyleClass().add("border");

        funcDisplay = new Label();
        funcDisplay.setPrefWidth(labelWidth);
        funcDisplay.setPrefHeight(defaultHeight);
        funcDisplay.setLayoutX(labelX);
        funcDisplay.setLayoutY(labelY);
        funcDisplay.setText(equationText);
        funcDisplay.getStyleClass().add("normal-text");

        pane.getChildren().add(funcDisplay);
        colorPicker = new RoundColorPicker(colorX,colorY,yPos, defaultColor, false,root,controller);
        pane.getChildren().add(colorPicker.displayButton);
        parent.getChildren().add(pane);
    }

    public void updateTransform() {
        colorPicker.recalcExtraPositions();
    }
    public void executeMenuOption(String option) {
        if(option.equals("edit")) {
            controller.editEquation(this);
        }
        if (option.equals("delete")) {
            controller.deleteEquation(this);
        }
    }
    public void setEquationText(String text) {
        this.equationText = text;
        this.funcDisplay.setText(text);
    }
}
