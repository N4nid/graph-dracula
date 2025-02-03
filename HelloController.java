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
import java.util.Random;

public class HelloController {
    public ArrayList<Hideble> hideOnClick = new ArrayList<Hideble>();
    public Label welcomeText;
    public ImageView graphView;
    public TextField equationInput;
    public Pane equationList;
    public Pane root;
    public Pane equationInputPane;
    public Button extraInputButton;
    public Button addButton;
    public ScrollPane scrollPane;
    ArrayList<EquationVisElement> listElements = new ArrayList<EquationVisElement>();
    public IntObj equationListSize  = new IntObj(0);
    public RundColorPicker mainColorPicker;

    @FXML
    protected void onAddButtonClick() {
        equationListSize.increment();
        int len = listElements.size();
        listElements.add(new EquationVisElement(null,equationInput.getText(),equationList,root,scrollPane,30 + len*100,equationListSize,mainColorPicker.colorIndex));
        hideOnClick.add(listElements.get(listElements.size() - 1).colorPicker);
        if (listElements.size() > 8) {
            equationList.setPrefHeight(equationList.getHeight() + 100);
        }
        equationInput.setText("");
        mainColorPicker.pickColor(new Random().nextInt(15));
        setInputBarColor(mainColorPicker.colorValue);
    }

    public void setup() {
        TwoDVec<Double> colorPickPos = new TwoDVec<Double>(1650.0,15.0);
        mainColorPicker = new RundColorPicker(colorPickPos.x,colorPickPos.y,equationInputPane.getLayoutX(),equationInputPane.getLayoutY(), 0,true,root,this);
        equationInputPane.getChildren().add(mainColorPicker.displayButton);
        hideOnClick.add(mainColorPicker);

        setInputBarColor(mainColorPicker.colorValue);
    }

    public void setInputBarColor(Color col) {
        String rgbCode = toRGBCode(col);
        equationInputPane.setStyle("-fx-border-color: " + toRGBCode(mainColorPicker.colorValue));
        addButton.setStyle("-fx-border-color: " + toRGBCode(mainColorPicker.colorValue));
        extraInputButton.setStyle("-fx-border-color: " + toRGBCode(mainColorPicker.colorValue));
    }

    public void hideRedundantElements() {
        for (int i = 0; i < hideOnClick.size(); i++) {
            hideOnClick.get(i).hide();
        }
    }

    private static String toRGBCode( Color color )
    {
        return String.format( "#%02X%02X%02X",
                (int)( color.getRed() * 255 ),
                (int)( color.getGreen() * 255 ),
                (int)( color.getBlue() * 255 ) );
    }

}