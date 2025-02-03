package com.example.graphdraculagui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class RundColorPicker {
    public Button displayButton;
    public Color colorValue;
    public Pane colorPickerWindow;
    private boolean isUp;
    private static final int panelHeight = 270;
    private static final int panelWidth = 270;
    private static final int panelDistance = 40;
    private static final int colorChoiceColloms = 4;
    private static final int colorChoiceRows = 4;
    private static final int colorButtonDistance = 60;
    private static Color[] selectableColors = new Color[16];

    public RundColorPicker(int xPos, int yPos, double absX, double absY, Color defaultColor, boolean isUp, Pane root) {
        //Setup of displayButton
        this.displayButton = new Button();
        this.displayButton.setLayoutX(xPos);
        this.displayButton.setLayoutY(yPos);
        displayButton.getStyleClass().add("color-picker");
        colorValue = defaultColor;
        displayButton.setStyle("-fx-background-color: " + toRGBCode(colorValue));
        displayButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                onClick();
            }
        });

        //Setup of displayWindow pane
        colorPickerWindow = new Pane();
        colorPickerWindow.setPrefHeight(panelHeight);
        colorPickerWindow.setPrefWidth(panelWidth);
        TwoDVec<Integer> panelPos = new TwoDVec<Integer>();
        if (isUp) { //The color selector window has to either pop up over the selector or left of it
            panelPos.setPos(xPos - (panelWidth / 2), yPos - panelHeight - panelDistance);
        }
        else {
            panelPos.setPos(xPos - panelWidth - panelDistance, yPos - (panelHeight / 2));
        }
        panelPos.setPos(panelPos.x + (int)absX, panelPos.y + (int)absY);
        panelPos = limitPos(panelPos,root);
        moveTo(panelPos,colorPickerWindow);
        colorPickerWindow.getStyleClass().add("border");
        colorPickerWindow.getStyleClass().add("black");
        colorPickerWindow.setVisible(false);

        //Setup of ColorPickButtons
        setupColors();
        int colorCounter = 0;
        for (int c = 0; c < colorChoiceColloms; c++) {
            for (int r = 0; r < colorChoiceRows; r++) {
                colorPickerWindow.getChildren().add(new ColorPickButton(20+c*colorButtonDistance,20+r*colorButtonDistance,selectableColors[colorCounter],this).displayButton);
                colorCounter++;
            }
        }
    }

    private TwoDVec<Integer> limitPos(TwoDVec<Integer> input, Pane scene) {
        input.x = Math.max(0, input.x);
        input.x = Math.min((int)scene.getWidth(), input.x);
        input.y = Math.max(0, input.y);
        input.y = Math.min((int)scene.getHeight(), input.y);
        return input;
    }

    private void moveTo(TwoDVec<Integer> pos, Node node) {
        node.setLayoutX(pos.x);
        node.setLayoutY(pos.y);
    }

    private void onClick() {
        colorPickerWindow.setVisible(!colorPickerWindow.isVisible());
    }

    public void pickColor(Color pickedColor) {
        this.colorValue = pickedColor;
        displayButton.setStyle("-fx-background-color: " + toRGBCode(colorValue));
    }

    private static String toRGBCode( Color color )
    {
        return String.format( "#%02X%02X%02X",
                (int)( color.getRed() * 255 ),
                (int)( color.getGreen() * 255 ),
                (int)( color.getBlue() * 255 ) );
    }

    private static void setupColors() {
        Color color1 = Color.web("#F24136");
        Color color2 = Color.web("#E91D62");
        Color color3 = Color.web("#9A27AE");
        Color color4 = Color.web("#653BB5");
        Color color5 = Color.web("#4150B1");
        Color color6 = Color.web("#1F96F3");
        Color color7 = Color.web("#00BAD3");
        Color color8 = Color.web("#009788");
        Color color9 = Color.web("#4CAE50");
        Color color10 = Color.web("#8BC04A");
        Color color11 = Color.web("#C9DD39");
        Color color12 = Color.web("#FEBF05");
        Color color13 = Color.web("#FF9700");
        Color color14 = Color.web("#FA571C");
        Color color15 = Color.web("#775547");
        Color color16 = Color.web("#5D7A8E");
        selectableColors = new Color[] {color1,color2,color3,color4,color5,color6,color7,color8,color9,color10,color11,color12,color13,color14,color15,color16};
    }
}
