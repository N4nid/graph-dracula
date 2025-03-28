import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;


public class ExpandMenu implements Hideble{
    public Pane background;
    public Pane contentPane;
    public ScrollPane contenScroll;
    public Region bottomTarget = new Region();
    public Region topTarget = new Region();
    private Label label;
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
    private boolean isAnimating = false;
    public int lastCaretPos = 0;
    private boolean isUp = false;

    public ExpandMenu(Pane root, TextField mainInputField, Button expandButton, Label label) {
        background = new Pane();
        contentPane = new Pane();
        contenScroll = new ScrollPane();
        contenScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contenScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contenScroll.relocate(3,3);
        contenScroll.getStyleClass().add("no-border");
        background.getStyleClass().add("black");
        background.getStyleClass().add("border");
        contentPane.getStyleClass().add("black");
        background.relocate(xMargin,0);
        background.setPrefHeight(height);
        background.setViewOrder(-1);
        contenScroll.setContent(contentPane);
        background.getChildren().add(contenScroll);
        root.getChildren().add(background);
        this.mainInputField = mainInputField;
        this.expandButton = expandButton;
        this.label = label;
        initiateButtons();

        mainInputField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                lastCaretPos = mainInputField.getCaretPosition();
            }
        });

        topTarget.localToParentTransformProperty().addListener((obs, oldVal, newVal) -> {
            if (!isAnimating) {
                background.setLayoutY(topTarget.getLayoutY()+370);
            }
        });
        background.widthProperty().addListener((obs, oldVal, newVal) -> {
            buttonColloms = calculateColloms();
            positionButtons();
        });
    }

    public void triggerButton(String inputString) {
        for (int i = 0; i < mathButtons.size(); i++) {
            if (mathButtons.get(i).inputString.equals(inputString)) {
                mathButtons.get(i).actionProcessor();
            }
        }
    }

    private int calculateColloms(){
        return  (int) ((background.getPrefWidth() + 10)/(standardButtonSize.x+buttonDistXY));
    }

    private void initiateButtons() {
        TwoDVec<Double> currentPos = new TwoDVec<Double>(buttonDistXY,buttonDistXY);
        mathButtons.add(initiateButton("f(x)","f(x)=" , -4));
        mathButtons.add(initiateButton("pi","pi"));
        mathButtons.add(initiateButton("e","e"));
        mathButtons.add(initiateButton("phi","phi"));
        mathButtons.add(initiateButton("square","^(2)"));
        mathButtons.add(initiateButton("exponent","^()",-1));
        mathButtons.add(initiateButton("sqrt","sqrt()",-1));
        mathButtons.add(initiateButton("root","root(,)",-2));
        mathButtons.add(initiateButton("abs","abs()",-1));
        mathButtons.add(initiateButton("mod","mod"));
        mathButtons.add(initiateButton("log","log(,)",-2));
        mathButtons.add(initiateButton("log1o","log(10,)",-1));
        mathButtons.add(initiateButton("ln","ln()",-1));
        mathButtons.add(initiateButton("e_exp","e^"));
        mathButtons.add(initiateButton("sin","sin()",-1));
        mathButtons.add(initiateButton("cos","cos()",-1));
        mathButtons.add(initiateButton("tan","tan()",-1));
        mathButtons.add(initiateButton("sin_ex","sin()^(-1)",-6));
        mathButtons.add(initiateButton("cos_ex","cos()^(-1)",-6));
        mathButtons.add(initiateButton("tan_ex","tan()^(-1)",-6));
        positionButtons();
    }

    private void positionButtons() {
        int buttonRows = (int) ((mathButtons.size()-1) / buttonColloms) + 1;
        contentPane.setPrefHeight(buttonRows*(standardButtonSize.y+buttonDistXY)+buttonDistXY);
        for (int i = 0; i < mathButtons.size(); i++) {
            TwoDVec<Integer> posIndex = new TwoDVec<Integer>(i % buttonColloms,(int) (i / buttonColloms));
            TwoDVec<Double> pos = new TwoDVec<Double>(buttonDistXY + posIndex.x * (standardButtonSize.x + buttonDistXY),buttonDistXY + posIndex.y * (standardButtonSize.y + buttonDistXY));
            mathButtons.get(i).setPos(pos);
        }
    }

    private void animateEntrance(boolean showUp) {
        isAnimating = true;
        if (showUp) {
            background.setVisible(true);
        }
        if (!showUp) {
            label.setVisible(false);
        }
        TranslateTransition transition = new TranslateTransition(Duration.millis(75),background);
        background.setLayoutY(bottomTarget.getLayoutY());
        transition.setByY((showUp)? (topTarget.getLayoutY()-bottomTarget.getLayoutY()) : -(topTarget.getLayoutY()-bottomTarget.getLayoutY()));
        transition.setCycleCount(1);
        transition.play();
        transition.setOnFinished(e ->{
            isAnimating = false;
            background.setVisible(showUp);
            label.setVisible(showUp);
            background.setLayoutY(topTarget.getLayoutY()+370);
        });
    }

    public boolean hide() {
        if (!background.hoverProperty().getValue() && !expandButton.hoverProperty().getValue() && !mainInputField.hoverProperty().getValue()) {
            if (background.isVisible()) {
                animateEntrance(false);
                isUp = false;
            }
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
        label.setVisible(false);
        isUp = false;
    }

    public void flipVisibility() {
        if(!isAnimating) {
            animateEntrance(!isUp);
            isUp = !isUp;
        }
    }
}

class MathButton {
    private ExpandMenu parentMenu;

    public String inputString;
    private int cursorPosOffset;

    private Button baseButton;

    public MathButton(ExpandMenu parentMenu, String displayImagePath, String inputString, int cursorPosOffset, TwoDVec<Double> scale) {
        baseButton = new Button();
        baseButton.getStyleClass().add("black");
        baseButton.getStyleClass().add("thin-border");
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
        parentMenu.contentPane.getChildren().add(baseButton);
    }

    public void actionProcessor() {
        TextField inputField =  parentMenu.mainInputField;
        inputField.requestFocus();
        inputField.positionCaret(parentMenu.lastCaretPos);
        inputField.insertText(inputField.getCaretPosition(),inputString);
        inputField.positionCaret(inputField.getCaretPosition() + cursorPosOffset);
    }



    public void setPos(TwoDVec<Double> pos) {
        baseButton.relocate(pos.x,pos.y);
    }
}
