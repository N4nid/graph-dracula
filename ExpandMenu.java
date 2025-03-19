import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import java.util.ArrayList;


public class ExpandMenu implements Hideble{
    public Pane background;
    public TextField mainInputField;
    private double xMargin = 60;
    private double height = 215;
    private TwoDVec<Double> standardButtonSize = new TwoDVec<Double>(99.875,52.0);
    private double buttonDistXY = 15;
    private int buttonColloms = 8;
    private ArrayList<MathButton> mathButtons = new ArrayList<MathButton>();
    private String standardPath = "/resources/MathIcons/";
    private String standardImageFormat = ".png";
    private Button expandButton;
    public int lastCaretPos = 0;

    public ExpandMenu(Pane root, TextField mainInputField, Button expandButton) {
        background = new Pane();
        background.getStyleClass().add("black");
        background.getStyleClass().add("border");
        background.relocate(xMargin,0);
        background.setPrefHeight(height);
        background.setViewOrder(-1);
        root.getChildren().add(background);
        this.mainInputField = mainInputField;
        this.expandButton = expandButton;
        initiateButtons();

        mainInputField.setOnMouseClicked(e ->{
            lastCaretPos = mainInputField.getCaretPosition();
        });
        mainInputField.setOnKeyPressed(e ->{
            if (e.getCode() == KeyCode.LEFT) {
                lastCaretPos--;
            }
            else {
                lastCaretPos++;
            }
        });
    }

    private void initiateButtons() {
        TwoDVec<Double> currentPos = new TwoDVec<Double>(buttonDistXY,buttonDistXY);
        mathButtons.add(initiateButton("f(x)","f(x) = " , -6));
        mathButtons.add(initiateButton("pi","pi"));
        mathButtons.add(initiateButton("e","e"));
        mathButtons.add(initiateButton("phi","phi"));
        mathButtons.add(initiateButton("square","^2"));
        mathButtons.add(initiateButton("exponent","^"));
        mathButtons.add(initiateButton("sqrt","sqrt()",-1));
        mathButtons.add(initiateButton("root","root(,)",-2));
        mathButtons.add(initiateButton("log","log(,)",-2));
        mathButtons.add(initiateButton("log1o","log(10,)",-1));
        mathButtons.add(initiateButton("ln","ln()",-1));
        mathButtons.add(initiateButton("e_exp","e^"));
        mathButtons.add(initiateButton("sin","sin()",-1));
        mathButtons.add(initiateButton("cos","cos()",-1));
        mathButtons.add(initiateButton("tan","tan()",-1));
        mathButtons.add(initiateButton("sin_ex","sin()^-1",-4));
        mathButtons.add(initiateButton("cos_ex","cos()^-1",-4));
        mathButtons.add(initiateButton("tan_ex","tan()^-1",-4));

        for (int i = 0; i < mathButtons.size(); i++) {
            TwoDVec<Integer> posIndex = new TwoDVec<Integer>(i % buttonColloms,(int) (i / buttonColloms));
            TwoDVec<Double> pos = new TwoDVec<Double>(buttonDistXY + posIndex.x * (standardButtonSize.x + buttonDistXY),buttonDistXY + posIndex.y * (standardButtonSize.y + buttonDistXY));
            mathButtons.get(i).setPos(pos);
        }
    }

    private void animateEntrance(boolean showUp) {
        background.setVisible(true);
        int yOffset = (showUp) ? 400 : -400;
    }

    public boolean hide() {
        if (!background.hoverProperty().getValue() && !expandButton.hoverProperty().getValue() && !mainInputField.hoverProperty().getValue()) {
            background.setVisible(false);
            return true;
        }
        return false;
    }

    private MathButton initiateButton(String filename, String inputString) {
        return new MathButton(this,standardPath + filename + standardImageFormat,inputString,0,standardButtonSize);
    }
    private MathButton initiateButton(String filename, String inputString, int cursorPos) {
        return new MathButton(this,standardPath + filename + standardImageFormat,inputString,cursorPos,standardButtonSize);
    }

    public void dissappear() {
        background.setVisible(false);
    }

    public void flipVisibility() {
        background.setVisible(!background.isVisible());
    }
}

class MathButton {
    private ExpandMenu parentMenu;

    private String inputString;
    private int cursorPosOffset;

    private Button baseButton;

    public MathButton(ExpandMenu parentMenu, String displayImagePath, String inputString, int cursorPosOffset, TwoDVec<Double> scale) {
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
        this.cursorPosOffset = cursorPosOffset;
        this.parentMenu = parentMenu;
        baseButton.setOnAction(e -> {
            actionProcessor();
        });
        parentMenu.background.getChildren().add(baseButton);
    }

    private void actionProcessor() {
        TextField inputField =  parentMenu.mainInputField;
        inputField.requestFocus();
        inputField.positionCaret(parentMenu.lastCaretPos);
        inputField.insertText(inputField.getCaretPosition(),inputString);
        inputField.positionCaret(inputField.getCaretPosition() + cursorPosOffset);
        parentMenu.lastCaretPos = inputField.getCaretPosition();
    }



    public void setPos(TwoDVec<Double> pos) {
        baseButton.relocate(pos.x,pos.y);
    }
}
