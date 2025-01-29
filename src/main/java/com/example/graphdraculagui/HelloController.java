package com.example.graphdraculagui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
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
    public Pane root;
    public ScrollPane scrollPane;
    ArrayList<EquationVisElement> listElements = new ArrayList<EquationVisElement>();

    @FXML
    protected void onAddButtonClick() {
        Image testImage = new Image("GraphDraculaSampleGraph.png");
        graphView.setImage(testImage);
        int len = listElements.size();
        listElements.add(new EquationVisElement(null,equationInput.getText(),equationList,root,scrollPane,30 + len*100));
        if (listElements.size() > 8) {
            equationList.setPrefHeight(equationList.getHeight() + 100);
        }
    }
}