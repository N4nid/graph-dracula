import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;

public class HelloController {
  public ArrayList<Hideble> hideOnClick = new ArrayList<Hideble>();
  public Label welcomeText;
  public ImageView graphView;
  public Pane graphViewPane;
  public TextField equationInput;
  public Pane equationList;
  public Label equationListLabel;
  public Label graphViewLabel;
  public Pane root;
  public Pane equationInputPane;
  public Button extraInputButton;
  public Button addButton;
  public ScrollPane scrollPane;
  ArrayList<EquationVisElement> listElements = new ArrayList<EquationVisElement>();
  public IntObj equationListSize  = new IntObj(0);
  public RundColorPicker mainColorPicker;
  public Scene scene;
  
  private static TwoDVec<Double> defaultAddButtonPos;
  private static TwoDVec<Double> defaultExpandButtonPos;
  private static TwoDVec<Double> defaultGraphViewPanePos;
  private static TwoDVec<Double> defaultGraphViewPaneSize;
  private static TwoDVec<Double> defaultScrollPaneSize;
  private static double defaultInputPaneLength;
  private static double viewListHorizontalRatio;
  private static double viewListHorizontalDist;

  private static final double defaultSceneHeight = 1080;
  private static final double defaultSceneWidth = 1920;
  private static final double defaultButtonSize = 70;

  private ArrayList<Anchor> anchors = new ArrayList<Anchor>();
  
  @FXML
  protected void onAddButtonClick() {
    equationListSize.increment();
    int len = listElements.size();
    EquationVisElement newElement = new EquationVisElement(null,equationInput.getText(),equationList,root,scrollPane,30 + len*100,equationListSize,mainColorPicker.colorIndex);
    listElements.add(newElement);
    hideOnClick.add(newElement.colorPicker);
    if (listElements.size() > 8) {
      equationList.setPrefHeight(equationList.getHeight() + 100);
    }
    equationInput.setText("");
    mainColorPicker.pickColor(new Random().nextInt(15));
    setInputBarColor(mainColorPicker.colorValue);
    anchors.add(new Anchor(newElement.pane,scrollPane,new TwoDVec<Double>(-46.0,0.0),"scale",false,true));
    anchors.get(anchors.size() - 1).applyAnchor();
    anchors.add(new Anchor(newElement.funcDisplay,newElement.pane,new TwoDVec<Double>(-76.0,0.0),"scale",false,true));
    anchors.get(anchors.size() - 1).applyAnchor();
  }
  
  public void setup() {
    TwoDVec<Double> colorPickPos = new TwoDVec<Double>(1650.0,15.0);
    mainColorPicker = new RundColorPicker(colorPickPos.x,colorPickPos.y,equationInputPane.getLayoutX(),equationInputPane.getLayoutY(), 0,true,root,this);
    equationInputPane.getChildren().add(mainColorPicker.displayButton);
    hideOnClick.add(mainColorPicker);
    
    setInputBarColor(mainColorPicker.colorValue);
    calculateDefaultSizes();
    scene = equationInput.getScene();

    anchors.add(new Anchor(equationInput,equationInputPane,new TwoDVec<Double>(-40.0,0.0),"scale",false,true));
    anchors.add(new Anchor(equationList,scrollPane,new TwoDVec<Double>(0.0,0.0),"scale"));
    anchors.add(new Anchor(graphViewLabel,graphViewPane,new TwoDVec<Double>(15.0,-13.0),"pos"));
    anchors.add(new Anchor(equationListLabel,scrollPane,new TwoDVec<Double>(15.0,-13.0),"pos"));

    resize();
    scene.widthProperty().addListener((obs, oldVal, newVal) -> {
      resize();
    });
    
    scene.heightProperty().addListener((obs, oldVal, newVal) -> {
      resize();
    });
  }
  
  public void calculateDefaultSizes() {
    defaultAddButtonPos = new TwoDVec<Double>(addButton.getLayoutX(), addButton.getLayoutY());
    defaultExpandButtonPos = new TwoDVec<Double>(extraInputButton.getLayoutX(), extraInputButton.getLayoutY());
    defaultInputPaneLength = equationInputPane.getWidth();
    defaultGraphViewPaneSize = new TwoDVec<Double>(graphViewPane.getWidth(),graphViewPane.getHeight());
    defaultGraphViewPanePos = new TwoDVec<Double>(graphViewPane.getLayoutX(),graphViewPane.getLayoutY());
    defaultScrollPaneSize = new TwoDVec<Double>(scrollPane.getWidth(),scrollPane.getHeight());
    viewListHorizontalRatio = graphViewPane.getWidth() / (scrollPane.getWidth() + graphViewPane.getWidth());
    viewListHorizontalDist = scrollPane.getLayoutX() - graphViewPane.getLayoutX() - graphViewPane.getWidth();
  }
  
  public void resize() {
    double screenWidth = scene.getWindow().getWidth();
    double screenHeight = scene.getWindow().getHeight();
    double vertDiff =  defaultSceneHeight - screenHeight;
    double horzDiff = defaultSceneWidth - screenWidth;
    
    TwoDVec<Double> expandButtonPos = new TwoDVec<Double>(defaultExpandButtonPos.x, defaultExpandButtonPos.y - vertDiff);
    TwoDVec<Double> addButtonPos = new TwoDVec<Double>(defaultAddButtonPos.x - horzDiff, defaultExpandButtonPos.y - vertDiff);
    TwoDVec<Double> inputPanePos = new TwoDVec<Double>(expandButtonPos.x + defaultButtonSize, expandButtonPos.y);
    TwoDVec<Double> graphViewPaneSize = new TwoDVec<Double>(defaultGraphViewPaneSize.x - viewListHorizontalRatio * horzDiff, defaultGraphViewPaneSize.y - vertDiff);
    TwoDVec<Double> scrollPanePos = new TwoDVec<Double>(graphViewPane.getLayoutX() + graphViewPaneSize.x + viewListHorizontalDist, defaultGraphViewPanePos.y);
    TwoDVec<Double> scrollPaneSize = new TwoDVec<Double>(defaultScrollPaneSize.x - (1-viewListHorizontalRatio) * horzDiff,defaultScrollPaneSize.y - vertDiff);
    double inputPaneLegth = defaultInputPaneLength - horzDiff;
    
    moveTo(expandButtonPos,extraInputButton);
    moveTo(addButtonPos,addButton);
    moveTo(inputPanePos,equationInputPane);
    moveTo(scrollPanePos,scrollPane);
    equationInputPane.setPrefWidth(inputPaneLegth);
    graphViewPane.setPrefWidth(graphViewPaneSize.x);
    graphViewPane.setPrefHeight(graphViewPaneSize.y);
    scrollPane.setPrefWidth(scrollPaneSize.x);
    scrollPane.setPrefHeight(scrollPaneSize.y);

    Anchor.applyAnchors(anchors);
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
  
  private void moveTo(TwoDVec<Double> pos, Node node) {
    node.setLayoutX(pos.x);
    node.setLayoutY(pos.y);
  }

  
  private static String toRGBCode( Color color )
  {
    return String.format( "#%02X%02X%02X",
    (int)( color.getRed() * 255 ),
    (int)( color.getGreen() * 255 ),
    (int)( color.getBlue() * 255 ) );
  }
  
}

class Anchor {
   private Region baseObject;
   private Region relateToObject;
   private TwoDVec<Double> offsetVec;
   private boolean keepX = false;
   private boolean keepY = false;
   private String Type;

   public Anchor(Region baseObject, Region relateToObject, TwoDVec<Double> offsetVec, String Type) {
     this.baseObject = baseObject;
     this.relateToObject = relateToObject;
     this.offsetVec = offsetVec;
     this.Type = Type;
   }

  public Anchor(Region baseObject, Region relateToObject, TwoDVec<Double> offsetVec, String Type, boolean keepX, boolean keepY) {
    this.baseObject = baseObject;
    this.relateToObject = relateToObject;
    this.offsetVec = offsetVec;
    this.keepX = keepX;
    this.keepY = keepY;
    this.Type = Type;
  }

   public void applyAnchor(){
     if (Type.equals("scale")) {;
       if (!keepX) {
         baseObject.setPrefWidth(relateToObject.getPrefWidth() + offsetVec.x);
       }
       if (!keepY) {
         baseObject.setPrefHeight(relateToObject.getPrefHeight() + offsetVec.y);
       }
     }
     if (Type.equals("pos")) {
       if (!keepX) {
         baseObject.setLayoutX(relateToObject.getLayoutX() + offsetVec.x);
       }
       if (!keepY) {
         baseObject.setLayoutY(relateToObject.getLayoutY() + offsetVec.y);
       }
     }
     if (Type.equals("scale->pos")) {
       if (!keepX) {
         baseObject.setLayoutX(relateToObject.getPrefWidth() + offsetVec.x);
       }
       if (!keepY) {
         baseObject.setLayoutY(relateToObject.getPrefHeight() + offsetVec.y);
       }
     }
     if (Type.equals("pos->scale")) {
       if (!keepX) {
         baseObject.setPrefWidth(relateToObject.getLayoutX() + offsetVec.x);
       }
       if (!keepY) {
         baseObject.setPrefHeight(relateToObject.getLayoutY() + offsetVec.y);
       }
     }
   }

   public static void applyAnchors(ArrayList<Anchor> anchors) {
     for (int i = 0; i < anchors.size(); i++) {
       anchors.get(i).applyAnchor();
     }
  }
}