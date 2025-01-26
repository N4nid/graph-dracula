package com.example.graphdraculagui;

import javafx.scene.control.Button;
import javafx.scene.paint.Color;

public class RundColorPicker {
    public Button displayButton;
    public Color colorValue;

    public RundColorPicker(int xPos, int yPos, Color defaultColor) {
        this.displayButton = new Button();
        this.displayButton.setLayoutX(xPos);
        this.displayButton.setLayoutY(yPos);
        displayButton.getStyleClass().add("color-picker");
        colorValue = defaultColor;
        displayButton.setStyle("-fx-background-color: " + toRGBCode(colorValue));
    }

    private static String toRGBCode( Color color )
    {
        return String.format( "#%02X%02X%02X",
                (int)( color.getRed() * 255 ),
                (int)( color.getGreen() * 255 ),
                (int)( color.getBlue() * 255 ) );
    }
}
