import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.util.ArrayList;


public class ExpandMenu {
    public Pane background;
    public TextField mainInputField;
    private double xMargin = 60;
    private double height = 200;
    private TwoDVec<Double> standardButtonSize = new TwoDVec<Double>(99.875,52.0);
    private double buttonDistXY = 20;
    private int buttonColloms = 5;
    private int buttonRows = 4;
    private ArrayList<MathButton> mathButtons = new ArrayList<MathButton>();
    private String standardPath = "/resources/MathIcons/";
    private String standardImageFormat = ".png";
    public ExpandMenu(Pane root) {
        background = new Pane();
        background.getStyleClass().add("black");
        background.getStyleClass().add("border");
        background.relocate(xMargin,0);
        background.setPrefHeight(height);
        background.setViewOrder(-1);
        root.getChildren().add(background);
        initiateButtons();
    }

    private void initiateButtons() {
        TwoDVec<Double> currentPos = new TwoDVec<Double>(buttonDistXY,buttonDistXY);
        mathButtons.add(initiateButton("f(X)","f(x) = " , -6));
        mathButtons.add(initiateButton("pi","pi"));
        mathButtons.add(initiateButton("e","e"));
        mathButtons.add(initiateButton("phi","phi"));
        mathButtons.add(initiateButton("square","^2"));
        mathButtons.add(initiateButton("exponent","^"));
        mathButtons.add(initiateButton("sqrt","sqrt()",-1));
        mathButtons.add(initiateButton("root","root(,)",-2));

    }

    private MathButton initiateButton(String filename, String inputString) {
        return new MathButton(this,standardPath + filename + standardImageFormat,inputString,0,standardButtonSize);
    }
    private MathButton initiateButton(String filename, String inputString, int cursorPos) {
        return new MathButton(this,standardPath + filename + standardImageFormat,inputString,cursorPos,standardButtonSize);
    }
}

class MathButton {
    private ExpandMenu parentMenu;

    private String inputString;
    private int cursorPos;

    public Button baseButton;

    public MathButton(ExpandMenu parentMenu, String displayImagePath, String inputString, int cursorPos, TwoDVec<Double> scale) {
        baseButton = new Button();
        baseButton.getStyleClass().add("black");
        baseButton.getStyleClass().add("border");
        baseButton.getStyleClass().add("image-button");
        baseButton.setStyle("-fx-background-image: url('" + displayImagePath + "');");
        baseButton.getStyleClass().add("math-button");
        baseButton.setPrefWidth(scale.x);
        baseButton.setPrefHeight(scale.y);
        Effects.addDefaultHoverEffect(baseButton);
        this.inputString = inputString;
        this.cursorPos = cursorPos;
        this.parentMenu = parentMenu;
        parentMenu.background.getChildren().add(baseButton);
    }


}
