//package com.example.graphdraculagui;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;

public class HelloController {
    public ArrayList<Hideble> hideOnClick = new ArrayList<Hideble>();
    public Label welcomeText;
    public ImageView graphView;
    public TextField equationInput;
    public Pane equationList;
    public Pane root;
    public Pane equationInputPane;
    public ScrollPane scrollPane;
    ArrayList<EquationVisElement> listElements = new ArrayList<EquationVisElement>();
    public IntObj equationListSize  = new IntObj(0);
    public RundColorPicker mainColorPicker;

    //public Node[]

    @FXML
    protected void onAddButtonClick() {
        equationListSize.increment();
        Image testImage = new Image("/resources/GraphDraculaSampleGraph.png");
        graphView.setImage(testImage);
        int len = listElements.size();
        listElements.add(new EquationVisElement(null,equationInput.getText(),equationList,root,scrollPane,30 + len*100,equationListSize));
        hideOnClick.add(listElements.getLast().colorPicker);
        if (listElements.size() > 8) {
            equationList.setPrefHeight(equationList.getHeight() + 100);
        }
    }

    public void setup() {
        TwoDVec<Double> colorPickPos = new TwoDVec<Double>(1650.0,15.0);
        mainColorPicker = new RundColorPicker(colorPickPos.x,colorPickPos.y,equationInputPane.getLayoutX(),equationInputPane.getLayoutY(), 0,true,root);
        equationInputPane.getChildren().add(mainColorPicker.displayButton);
        hideOnClick.add(mainColorPicker);
    }


    public void hideRedundantElements() {
        for (int i = 0; i < listElements.size(); i++) {
            hideOnClick.get(i).hide();
        }
    }

}