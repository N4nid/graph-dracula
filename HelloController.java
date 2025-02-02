//package com.example.graphdraculagui;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

import java.util.ArrayList;

public class HelloController {
    public ArrayList<Hideble> hideOnClick = new ArrayList<Hideble>();
    public Label welcomeText;
    public ImageView graphView;
    public TextField equationInput;
    public Pane equationList;
    public Pane root;
    public ScrollPane scrollPane;
    ArrayList<EquationVisElement> listElements = new ArrayList<EquationVisElement>();
    public IntObj equationListSize  = new IntObj(0);

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


    public void hideRedundantElements() {
        for (int i = 0; i < listElements.size(); i++) {
            hideOnClick.get(i).hide();
        }
    }

}