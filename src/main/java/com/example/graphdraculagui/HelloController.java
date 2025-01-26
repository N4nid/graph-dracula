package com.example.graphdraculagui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public class HelloController {
    public Label welcomeText;
    public ImageView graphView;
    public TextField equationInput;
    public Pane equationList;
    ArrayList<EquationVisElement> listElements = new ArrayList<EquationVisElement>();

    @FXML
    protected void onAddButtonClick() {
        Image testImage = new Image("GraphDraculaSampleGraph.png");
        graphView.setImage(testImage);
        int len = listElements.size();
        listElements.add(new EquationVisElement(null,equationInput.getText(),equationList,30 + len*100));

    }
}