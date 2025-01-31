//package com.example.graphdraculagui;


import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("GraphDraculaUI.fxml"));
        //FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("GraphDraculaUI.fxml"));
        //Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
        Scene scene = new Scene(root);
        String css = this.getClass().getResource("application.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }



    public static EquationTree buildTestEquation() { // 2+4*4
        EquationNode root = new EquationNode((byte) 2,"+");
        root.left = new EquationNode((byte) 0,3);
        root.right = new EquationNode((byte) 2,"*");
        root.right.left = new EquationNode((byte) 0,4);
        root.right.right = new EquationNode((byte) 0,4);
        return new EquationTree(root);
    }

    public static EquationTree buildTestFunction() { //0 = x^2 -y
        EquationNode root = new EquationNode((byte) 2,"-");
        root.left = new EquationNode((byte) 2,"^");
        root.right = new EquationNode((byte) 1,"y");
        root.left.left = new EquationNode((byte) 1,"x");
        root.left.right = new EquationNode((byte) 0,2);
        return new EquationTree(root);
    }

    public static EquationTree buildComplicatedTestFunction() { //0 = ln(sin(sqrt(2x))-y
        EquationNode root = new EquationNode((byte) 2,"-");
        root.left = new EquationNode((byte) 3,"ln");
        root.right = new EquationNode((byte) 1,"y");
        root.left.left = new EquationNode((byte) 3,"sin");
        root.left.left.left = new EquationNode((byte) 2,"root");
        root.left.left.left.left = new EquationNode((byte) 0,2);
        root.left.left.left.right = new EquationNode((byte) 2,"*");
        root.left.left.left.right.left = new EquationNode((byte) 1,"x");
        root.left.left.left.right.right = new EquationNode((byte) 0,2);
        return new EquationTree(root);
    }

    public static EquationTree buildTestKreis() { //0 = y^2 + x^2 - 9
        EquationNode root = new EquationNode((byte) 2,"-");
        root.left = new EquationNode((byte) 2,"+");
        root.right = new EquationNode((byte) 0,9);
        root.left.left = new EquationNode((byte) 2,"^");
        root.left.right = new EquationNode((byte) 2,"^");
        root.left.left.left = new EquationNode((byte) 1,"y");
        root.left.left.right = new EquationNode((byte) 0,2);
        root.left.right.left = new EquationNode((byte) 1,"x");
        root.left.right.right = new EquationNode((byte) 0,2);
        return new EquationTree(root);
    }

    public static EquationTree buildTestParameterFunction() { //0 = ax^2 - y
        EquationNode root = new EquationNode((byte) 2,"-");
        root.left = new EquationNode((byte) 2,"*");
        root.right = new EquationNode((byte) 1,"y");
        root.left.left = new EquationNode((byte) 1,"a");
        root.left.right = new EquationNode((byte) 2,"^");
        root.left.right.left = new EquationNode((byte) 1,"x");
        root.left.right.right = new EquationNode((byte) 0,2);
        return new EquationTree(root);
    }

    public static void main(String[] args) {
        launch();
    }
}