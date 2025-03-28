import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;

public class ApplicationController implements MenuHaver {
  public ArrayList<Hideble> hideOnClick = new ArrayList<Hideble>();
  public Label welcomeText;
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
  public Label expandMenuLabel;
  private Button previewButton = new Button();
  private MenuOption recenterButton;
  private ExpandMenu expandMenu;
  ArrayList<EquationVisElement> listElements = new ArrayList<EquationVisElement>();
  
  public RoundColorPicker mainColorPicker;
  public Renderer renderer;
  
  public Scene scene;
  
  public double minEquationListHeight = 20f;
  public int editIndex = -1;
  public EquationTree previewEquation;
  
  private static TwoDVec<Double> defaultGraphViewPanePos;
  private static TwoDVec<Double> defaultGraphViewPaneSize;
  private static TwoDVec<Double> defaultScrollPaneSize;
  private static double viewListHorizontalRatio;
  private static double viewListHorizontalDist;
  
  private static final double defaultSceneHeight = 1080;
  private static final double defaultSceneWidth = 1920;
  private static final double defaultButtonSize = 70;
  public static double zoomSensitivity = 0.0015;

  private static final KeyCharacterCombination insertFunctionShortcut = new KeyCharacterCombination("f",KeyCharacterCombination.CONTROL_DOWN);
  private static final KeyCodeCombination goToLineEndShortcut = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);
  private static final KeyCharacterCombination expandMenuShortcut = new KeyCharacterCombination("e",KeyCharacterCombination.CONTROL_DOWN);
  private static final KeyCodeCombination closeWindowShortcut = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
  
  private ArrayList<Anchor> anchors = new ArrayList<Anchor>();
  private static TwoDVec<Double> mouseMindpointOffset;
  boolean firstDrag = true;
  
  @FXML
  protected void onAddButtonClick() {
    previewEquation = null;
    EquationTree inputEquation = EquationParser.parseString(equationInput.getText());
    if (inputEquation.root == null) {
      System.out.println("Invalid equation! Please try again.");
      return;
    }
    if (!inputEquation.isFunction) {
      renderer.refreshEquationRenderer();
    }
    if (editIndex == -1) {
      addEquation(inputEquation, equationInput.getText(), mainColorPicker.colorIndex);
    } else {
      listElements.get(editIndex).setEquationText(equationInput.getText());
      listElements.get(editIndex).equation = inputEquation;
      listElements.get(editIndex).colorPicker.pickColor(mainColorPicker.colorIndex);
      editIndex = -1;
    }
    equationInput.setText("");
    mainColorPicker.pickColor(new Random().nextInt(15));
    setEditModeUI(false);
    updateInputBarColor();
    updateRenderCanvas();
  }
  
  public void addEquation(EquationTree equation, String equationText, int colorIndex) {
    int len = listElements.size();
    EquationVisElement newElement = new EquationVisElement(equation, equationText, equationList, root, scrollPane, 30 + len * 100, this, colorIndex);
    listElements.add(newElement);
    hideOnClick.add(newElement.colorPicker);
    minEquationListHeight += 100;
    if (equationList.getPrefHeight() < minEquationListHeight) {
      equationList.setPrefHeight(minEquationListHeight);
    }
    anchors.add(new Anchor(newElement.pane, scrollPane, new TwoDVec<Double>(-46.0, 0.0), "scale", false, true));
    anchors.get(anchors.size() - 1).applyAnchor();
    anchors.add(new Anchor(newElement.funcDisplay, newElement.pane, new TwoDVec<Double>(-76.0, 0.0), "scale", false, true));
    anchors.get(anchors.size() - 1).applyAnchor();
    
  }
  
  public void setup() {

    CustomVarUIElement test = new CustomVarUIElement("a",root,20);
    
    TwoDVec<Double> colorPickPos = new TwoDVec<Double>(1650.0, 15.0);
    mainColorPicker = new RoundColorPicker(colorPickPos.x, colorPickPos.y, 0, new Random().nextInt(15), true, root, this);
    equationInputPane.getChildren().add(mainColorPicker.displayButton);
    hideOnClick.add(mainColorPicker);
    
    expandMenu = new ExpandMenu(root,equationInput,extraInputButton,expandMenuLabel);
    hideOnClick.add(expandMenu);
    expandMenu.dissappear();
    
    graphViewLabel.setViewOrder(-1);
    expandMenuLabel.setViewOrder(-2);
    
    Effects.addDefaultHoverEffect(addButton);
    Effects.addDefaultHoverEffect(extraInputButton);
    updateInputBarColor();
    calculateDefaultSizes();
    scene = equationInput.getScene();

    renderer = new Renderer(this);
    renderer.mainCanvas = new Canvas(graphViewPane.getPrefWidth(), graphViewPane.getPrefHeight());
    renderer.mainCanvas.relocate(graphViewPane.getLayoutX(), graphViewPane.getLayoutY());
    GraphicsContext gc = renderer.mainCanvas.getGraphicsContext2D();
    root.getChildren().add(renderer.mainCanvas);    
    mouseMindpointOffset = new TwoDVec<Double>(0.0, 0.0);
    recenterButton = new MenuOption("recenter", new Image("/resources/recenter.png"), 15, 20, this, new TwoDVec<Double>(135.0, 30.0), new TwoDVec<Double>(200.0, 200.0), root);
    recenterButton.optionPane.setVisible(false);
    previewButton.setPrefHeight(defaultButtonSize + 3);
    previewButton.setPrefWidth(defaultButtonSize + 3);
    previewButton.setVisible(false);
    previewButton.getStyleClass().add("black");
    previewButton.getStyleClass().add("border");
    previewButton.getStyleClass().add("image-button");
    previewButton.getStyleClass().add("preview-button");
    Effects.addDefaultHoverEffect(previewButton);
    root.getChildren().add(previewButton);
    previewButton.setOnAction(e -> addPreviewEquation());
    
    anchors.add(new Anchor(extraInputButton, root, new TwoDVec<Double>(0.0, -138.0), "scale->pos", true, false));
    anchors.add(new Anchor(addButton, root, new TwoDVec<Double>(-98.0, -138.0), "scale->pos"));
    anchors.add(new Anchor(equationInputPane, root, new TwoDVec<Double>(-226.0, 0.0), "scale", false, true));
    anchors.add(new Anchor(equationInputPane, extraInputButton, new TwoDVec<Double>(defaultButtonSize, 0.0), "pos"));
    anchors.add(new Anchor(equationInput, equationInputPane, new TwoDVec<Double>(-50.0, 0.0), "scale", false, true));
    anchors.add(new Anchor(mainColorPicker.displayButton, equationInput, new TwoDVec<Double>(0.0, 0.0), "scale->pos", false, true));
    anchors.add(new Anchor(equationList, scrollPane, new TwoDVec<Double>(0.0, 0.0), "scale"));
    anchors.add(new Anchor(graphViewLabel, graphViewPane, new TwoDVec<Double>(15.0, -13.0), "pos"));
    anchors.add(new Anchor(equationListLabel, scrollPane, new TwoDVec<Double>(15.0, -13.0), "pos"));
    anchors.add(new Anchor(recenterButton.optionPane, graphViewPane, new TwoDVec<Double>(0.0, 0.0), "pos"));
    anchors.add(new Anchor(recenterButton.optionPane, graphViewPane, new TwoDVec<Double>(-90.0, 0.0), "scale->pos"));
    anchors.add(new Anchor(previewButton, equationInputPane, new TwoDVec<Double>(0.0, 0.0), "pos"));
    anchors.add(new Anchor(previewButton, equationInputPane, new TwoDVec<Double>(128.0, 0.0), "scale->pos", false, true));
    anchors.add(new Anchor(expandMenu.background,root,new TwoDVec<Double>(-90.0,0.0),"scale",false,true,new TwoDVec<Double>(935.0,1000.0)));
    anchors.add(new Anchor(expandMenu.topTarget,root,new TwoDVec<Double>(0.0,-370.0),"scale->pos",true,false));
    anchors.add(new Anchor(expandMenu.bottomTarget,root,new TwoDVec<Double>(0.0,2.0),"scale->pos",true,false));
    anchors.add(new Anchor(expandMenu.contenScroll, expandMenu.background, new TwoDVec<Double>(-6.0, -6.0), "scale"));
    anchors.add(new Anchor(expandMenu.contentPane, expandMenu.contenScroll, new TwoDVec<Double>(0.0, 0.0), "scale",false,true));
    anchors.add(new Anchor(expandMenuLabel, expandMenu.background, new TwoDVec<Double>(15.0, -385.0), "pos"));
    resize();
    
    renderer.centerCoordinateSystem();
    updateRenderCanvas();
    scene.widthProperty().addListener((obs, oldVal, newVal) -> {
      resize();
    });
    
    scene.heightProperty().addListener((obs, oldVal, newVal) -> {
      resize();
    });
    
    scene.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER && equationInput.isFocused()) {
        onAddButtonClick();
      }
      if (insertFunctionShortcut.match(e)) {
        expandMenu.triggerButton("f(x)=");
      }
      if (goToLineEndShortcut.match(e)) {
        equationInput.positionCaret(equationInput.getText().length());
      }
      if (expandMenuShortcut.match(e)) {
        expandMenu.flipVisibility();
      }
      if (closeWindowShortcut.match(e)) {
        Stage window = (Stage) equationInput.getScene().getWindow();
        window.close();
      }
    });
    
    scene.setOnMouseClicked(e -> {
      if (e.getButton() == MouseButton.SECONDARY) {
        if (renderer.mainCanvas.isHover()) {
          TwoDVec<Double> mousePos = new TwoDVec<Double>(e.getX(), e.getY());
          OverlayMenu rightClickMenu = new OverlayMenu(this, "graphView", mousePos, root);
          hideOnClick.add(rightClickMenu);
        } else {
          EquationVisElement hoveringElement = getHoveredEquationVisElement();
          if (hoveringElement != null) {
            TwoDVec<Double> mousePos = new TwoDVec<Double>(e.getX(), e.getY());
            OverlayMenu rightClickMenu = new OverlayMenu(hoveringElement, "equationElement", mousePos, root);
            hideOnClick.add(rightClickMenu);
          }
        }
      }
    });
    
    scrollPane.setOnScroll(scrollEvent -> updateListElementTransform());
    
    renderer.mainCanvas.setOnScroll(scrollEvent -> {;
      double avgZoom = (renderer.renderValues.zoom.x + (renderer.renderValues.zoom.y) / 2);
      renderer.renderValues.zoom.setPos(renderer.renderValues.zoom.x - avgZoom * scrollEvent.getDeltaY() * zoomSensitivity,
      renderer.renderValues.zoom.y - avgZoom * scrollEvent.getDeltaY() * zoomSensitivity);
      updateRenderCanvas();
    });
    
    renderer.mainCanvas.setOnMouseReleased(e -> {
      firstDrag = true;
    });
    renderer.mainCanvas.setOnMouseDragged(e -> {
      if (firstDrag) {
        mouseMindpointOffset = new TwoDVec<Double>((e.getX() - renderer.renderValues.midpoint.x),
        e.getY() - renderer.renderValues.midpoint.y);
        firstDrag = false;
      }
      TwoDVec<Double> newPos = new TwoDVec<Double>((e.getX() - mouseMindpointOffset.x),
      (e.getY() - mouseMindpointOffset.y));
      if (graphOffsetInBounds(0.1, renderer.renderValues)) {
        recenterButton.optionPane.setVisible(false);
      } else {
        recenterButton.optionPane.setVisible(true);
      }
      renderer.renderValues.midpoint.setPos(newPos.x, newPos.y);
      updateRenderCanvas();
    });
    
    extraInputButton.setOnAction(e->{
      expandMenu.flipVisibility();
    });
    
  }

  public EquationVisElement getHoveredEquationVisElement() {
    for (int i = 0; i < listElements.size(); i++) {
      if (listElements.get(i).pane.hoverProperty().getValue()) {
        return listElements.get(i);
      }
    }
    return null;
  }

  public static boolean graphOffsetInBounds(double margin, RenderValues renderValues) {
    double minX = renderValues.resolution.x * margin;
    double maxX = renderValues.resolution.x - renderValues.resolution.x * margin;
    double minY = renderValues.resolution.y * margin;
    double maxY = renderValues.resolution.y - renderValues.resolution.y * margin;
    
    boolean isInXBounds = renderValues.midpoint.x > minX && renderValues.midpoint.x < maxX;
    boolean isInYBounds = renderValues.midpoint.y > minY && renderValues.midpoint.y < maxY;
    
    return isInXBounds && isInYBounds;
  }

  public void executeMenuOption(String menuOption) {
    if (menuOption.equals("recenter")) {
      renderer.renderValues.midpoint.setPos((double)(renderer.renderValues.resolution.x / 2), (double)(renderer.renderValues.resolution.y / 2));
      updateRenderCanvas();
      recenterButton.optionPane.setVisible(false);
    }
    if (menuOption.equals("reset zoom")) {
      renderer.renderValues.zoom.setUniform(0.01);
      updateRenderCanvas();
    }
  }

  public void calculateDefaultSizes() {
    defaultGraphViewPaneSize = new TwoDVec<Double>(graphViewPane.getWidth(), graphViewPane.getHeight());
    defaultGraphViewPanePos = new TwoDVec<Double>(graphViewPane.getLayoutX(), graphViewPane.getLayoutY());
    defaultScrollPaneSize = new TwoDVec<Double>(scrollPane.getWidth(), scrollPane.getHeight());
    viewListHorizontalRatio = graphViewPane.getWidth() / (scrollPane.getWidth() + graphViewPane.getWidth());
    viewListHorizontalDist = scrollPane.getLayoutX() - graphViewPane.getLayoutX() - graphViewPane.getWidth();
  }

  public void resize() {
    double screenWidth = scene.getWindow().getWidth();
    double screenHeight = scene.getWindow().getHeight();
    double vertDiff = defaultSceneHeight - screenHeight;
    double horzDiff = defaultSceneWidth - screenWidth;
    
    root.setPrefWidth(root.getWidth());
    root.setPrefHeight(root.getHeight());
    
    TwoDVec<Double> graphViewPaneSize = new TwoDVec<Double>(
    defaultGraphViewPaneSize.x - viewListHorizontalRatio * horzDiff, defaultGraphViewPaneSize.y - vertDiff);
    TwoDVec<Double> scrollPanePos = new TwoDVec<Double>(
    graphViewPane.getLayoutX() + graphViewPaneSize.x + viewListHorizontalDist, defaultGraphViewPanePos.y);
    TwoDVec<Double> scrollPaneSize = new TwoDVec<Double>(
    defaultScrollPaneSize.x - (1 - viewListHorizontalRatio) * horzDiff, defaultScrollPaneSize.y - vertDiff);
    
    moveTo(scrollPanePos, scrollPane);
    graphViewPane.setPrefWidth(graphViewPaneSize.x);
    graphViewPane.setPrefHeight(graphViewPaneSize.y);
    scrollPane.setPrefWidth(scrollPaneSize.x);
    scrollPane.setPrefHeight(scrollPaneSize.y);
    updateRenderCanvas();
    Anchor.applyAnchors(anchors);
    if (equationList.getPrefHeight() < minEquationListHeight) {
      equationList.setPrefHeight(minEquationListHeight);
    }
    
    updateListElementTransform();
  }

  public void updateRenderCanvas() {
    renderer.mainCanvas.setWidth(graphViewPane.getPrefWidth() - 6);
    renderer.mainCanvas.setHeight(graphViewPane.getPrefHeight() - 6);
    renderer.mainCanvas.relocate(graphViewPane.getLayoutX()+3, graphViewPane.getLayoutY()+3);
    TwoDVec<Integer> res = new TwoDVec<Integer>((int) renderer.mainCanvas.getWidth(), (int) renderer.mainCanvas.getHeight());
    GraphicsContext gc = renderer.mainCanvas.getGraphicsContext2D();
    renderer.renderValues.resolution = res;
    
    renderer.renderEquations(listElements,previewEquation);
  }

  private void updateListElementTransform() {
    for (int i = 0; i < listElements.size(); i++) {
      int yPos = 30 + i * 100;
      listElements.get(i).pane.setLayoutY(yPos);
      listElements.get(i).updateTransform();
    }
  }

  public void deleteEquation(EquationVisElement equation) {
    equationList.getChildren().remove(equation.pane);
    listElements.remove(equation);
    minEquationListHeight -= 100;
    resize();
  }

  public void editEquation(EquationVisElement equation) {
    mainColorPicker.pickColor(equation.colorPicker.colorIndex);
    equationInput.setText(equation.equationText);
    editIndex = listElements.indexOf(equation);
    setEditModeUI(true);
  }

  public void addPreviewEquation() {
    EquationTree previewEquation = EquationParser.parseString(equationInput.getText());
    if (previewEquation.root == null) {
      System.out.println("Invalid equation, try again!");
    } else {
      this.previewEquation = previewEquation;
      updateRenderCanvas();
    }
  }

  public void setEditModeUI(boolean isEditMode) {
    if (isEditMode) {
      addButton.setStyle("-fx-background-image: url('/resources/checkmark.png');");
      Anchor equationInputPaneAnchor = Anchor.findAnchorOfObject(equationInputPane, "scale", anchors);
      equationInputPaneAnchor.offsetVec.setPos(-293.0, equationInputPaneAnchor.offsetVec.y);
      Anchor.applyAnchors(anchors);
      updateInputBarColor();
      previewButton.setVisible(true);
    } else {
      addButton.setStyle("-fx-background-image: url('/resources/addButton.png');");
      Anchor equationInputPaneAnchor = Anchor.findAnchorOfObject(equationInputPane, "scale", anchors);
      equationInputPaneAnchor.offsetVec.setPos(-226.0, equationInputPaneAnchor.offsetVec.y);
      Anchor.applyAnchors(anchors);
      updateInputBarColor();
      previewButton.setVisible(false);
    }
  }

  public void updateInputBarColor() {
    equationInputPane.setStyle("-fx-border-color: " + toRGBCode(mainColorPicker.colorValue));
    addButton.setStyle("-fx-border-color: " + toRGBCode(mainColorPicker.colorValue));
    extraInputButton.setStyle("-fx-border-color: " + toRGBCode(mainColorPicker.colorValue));
    previewButton.setStyle("-fx-border-color: " + toRGBCode(mainColorPicker.colorValue));
  }

  public void hideRedundantElements() {
    for (int i = 0; i < hideOnClick.size(); i++) {
      boolean hidden = hideOnClick.get(i).hide();
      if (hidden && hideOnClick.get(i) instanceof OverlayMenu) {
        destroyMenu((OverlayMenu) hideOnClick.get(i));
      }
    }
  }

  public void destroyMenu(OverlayMenu menu) {
    root.getChildren().remove(menu.window);
    hideOnClick.remove(menu);
  }

  private void moveTo(TwoDVec<Double> pos, Node node) {
    node.setLayoutX(pos.x);
    node.setLayoutY(pos.y);
  }

  private static String toRGBCode(Color color) {
    return String.format("#%02X%02X%02X",
    (int) (color.getRed() * 255),
    (int) (color.getGreen() * 255),
    (int) (color.getBlue() * 255));
  }

}
