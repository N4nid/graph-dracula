//package com.example.graphdraculagui;

import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class EquationVisElement {
    //Javafx-Elements
    private Pane pane;
    private Label funcDisplay;
    public RundColorPicker colorPicker;

    //Data
    public EquationTree equation;
    private String equationText;

    //Default-Values
    private static final int defaultXPos = 23;
    private static final int defaultWidth = 358;
    private static final int defaultHeight = 73;
    private static final int labelWidth = 290;
    private static final int labelX = 69;
    private static final int labelY = 0;
    private static final int colorX = 13;
    private static final int colorY = 14;

    public EquationVisElement(EquationTree equation, String equationText, Pane parent, Pane root, ScrollPane scrollpane, int yPos, IntObj equationListSize) {
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

        double absX = pane.getLayoutX() + scrollpane.getLayoutX();
        double absY = pane.getLayoutY() + scrollpane.getLayoutY();
        pane.getChildren().add(funcDisplay);
        colorPicker = new RundColorPicker(colorX,colorY,absX,absY, Color.ANTIQUEWHITE, false,root,scrollpane,equationListSize);
        pane.getChildren().add(colorPicker.displayButton);
        parent.getChildren().add(pane);
    }
}
