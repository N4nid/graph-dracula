import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
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
  private Button previewButton = new Button();
  private MenuOption recenterButton;
  public Canvas mainCanvas;
  private ExpandMenu expandMenu;
  ArrayList<EquationVisElement> listElements = new ArrayList<EquationVisElement>();

  public RoundColorPicker mainColorPicker;

  public Scene scene;

  public double minEquationListHeight = 20f;
  private int editIndex = -1;
  private EquationTree previewEquation;

  private static TwoDVec<Double> defaultGraphViewPanePos;
  private static TwoDVec<Double> defaultGraphViewPaneSize;
  private static TwoDVec<Double> defaultScrollPaneSize;
  private static double viewListHorizontalRatio;
  private static double viewListHorizontalDist;

  private static final double defaultSceneHeight = 1080;
  private static final double defaultSceneWidth = 1920;
  private static final double defaultButtonSize = 70;
  public static double zoomSensitivity = 0.0015;

  private ArrayList<Anchor> anchors = new ArrayList<Anchor>();
  private FunctionRenderer funcDrawer = new FunctionRenderer(new TwoDVec<Integer>(1920, 1080), new TwoDVec<Double>(0.02, 0.02), new TwoDVec<Double>(0.0, 0.0));
  private EquationRenderer equationRenderer;
  private static TwoDVec<Double> mouseMindpointOffset;
  boolean firstDrag = true;

  @FXML
  protected void onAddButtonClick() {
    equationRenderer.lastZoom = new TwoDVec<Double>(-1.0, -1.0);
    previewEquation = null;
    EquationTree inputEquation = EquationParser.parseString(equationInput.getText());
    if (inputEquation.root == null) {
      System.out.println("Invalid equation! Please try again.");
      return;
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


    TwoDVec<Double> colorPickPos = new TwoDVec<Double>(1650.0, 15.0);
    mainColorPicker = new RoundColorPicker(colorPickPos.x, colorPickPos.y, 0, new Random().nextInt(15), true, root, this);
    equationInputPane.getChildren().add(mainColorPicker.displayButton);
    hideOnClick.add(mainColorPicker);

    expandMenu = new ExpandMenu(root,equationInput,extraInputButton);
    hideOnClick.add(expandMenu);
    expandMenu.dissappear();

    graphViewLabel.setViewOrder(-1);

    Effects.addDefaultHoverEffect(addButton);
    Effects.addDefaultHoverEffect(extraInputButton);
    updateInputBarColor();
    calculateDefaultSizes();
    scene = equationInput.getScene();

    mainCanvas = new Canvas(graphViewPane.getPrefWidth(), graphViewPane.getPrefHeight());
    mainCanvas.relocate(graphViewPane.getLayoutX(), graphViewPane.getLayoutY());
    GraphicsContext gc = mainCanvas.getGraphicsContext2D();
    root.getChildren().add(mainCanvas);
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
    resize();

    funcDrawer.centerCoordinateSystem();
    equationRenderer = new EquationRenderer((int) graphViewPane.getPrefWidth(), (int) graphViewPane.getPrefHeight(), funcDrawer.zoom.x);
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
    });

    scene.setOnMouseClicked(e -> {
      if (e.getButton() == MouseButton.SECONDARY) {
        if (mainCanvas.isHover()) {
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

    mainCanvas.setOnScroll(scrollEvent -> {
      double avgZoom = (funcDrawer.zoom.x + funcDrawer.zoom.y) / 2;
      funcDrawer.zoom.setPos(funcDrawer.zoom.x - avgZoom * scrollEvent.getDeltaY() * zoomSensitivity,
              funcDrawer.zoom.y - avgZoom * scrollEvent.getDeltaY() * zoomSensitivity);
      updateRenderCanvas();
    });

    mainCanvas.setOnMouseReleased(e -> {
      firstDrag = true;
    });
    mainCanvas.setOnMouseDragged(e -> {
      if (firstDrag) {
        mouseMindpointOffset = new TwoDVec<Double>((e.getX() - funcDrawer.midpoint.x),
                e.getY() - funcDrawer.midpoint.y);
        firstDrag = false;
      }
      TwoDVec<Double> newPos = new TwoDVec<Double>((e.getX() - mouseMindpointOffset.x), (e.getY() - mouseMindpointOffset.y));
      if (graphOffsetInBounds(0.1, funcDrawer)) {
        recenterButton.optionPane.setVisible(false);
      } else {
        recenterButton.optionPane.setVisible(true);
      }
      funcDrawer.midpoint.setPos(newPos.x, newPos.y);
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

  public static boolean graphOffsetInBounds(double margin, FunctionRenderer funcDrawer) {
    double minX = funcDrawer.resolution.x * margin;
    double maxX = funcDrawer.resolution.x - funcDrawer.resolution.x * margin;
    double minY = funcDrawer.resolution.y * margin;
    double maxY = funcDrawer.resolution.y - funcDrawer.resolution.y * margin;

    boolean isInXBounds = funcDrawer.midpoint.x > minX && funcDrawer.midpoint.x < maxX;
    boolean isInYBounds = funcDrawer.midpoint.y > minY && funcDrawer.midpoint.y < maxY;

    return isInXBounds && isInYBounds;
  }

  public void executeMenuOption(String menuOption) {
    if (menuOption.equals("recenter")) {
      funcDrawer.centerCoordinateSystem();
      updateRenderCanvas();
      recenterButton.optionPane.setVisible(false);
    }
    if (menuOption.equals("reset zoom")) {
      funcDrawer.zoom.setUniform(0.01);
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
    mainCanvas.setWidth(graphViewPane.getPrefWidth() - 6);
    mainCanvas.setHeight(graphViewPane.getPrefHeight() - 6);
    mainCanvas.relocate(graphViewPane.getLayoutX()+3, graphViewPane.getLayoutY()+3);
    TwoDVec<Integer> res = new TwoDVec<Integer>((int) mainCanvas.getWidth(), (int) mainCanvas.getHeight());
    long startTime = System.nanoTime();
    GraphicsContext gc = mainCanvas.getGraphicsContext2D();
    gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
    funcDrawer.resolution = res;

    ArrayList<EquationTree> allEquations = new ArrayList<EquationTree>();
    ArrayList<EquationTree> equations = new ArrayList<EquationTree>();
    ArrayList<EquationTree> functions = new ArrayList<EquationTree>();
    for (int i = 0; i < listElements.size(); i++) {
      if (!(previewEquation != null && i == editIndex)) {
        allEquations.add(listElements.get(i).equation);
        allEquations.get(allEquations.size() - 1).graphColor = listElements.get(i).colorPicker.colorValue;
      } else {
        allEquations.add(EquationParser.parseString(equationInput.getText()));
        allEquations.get(allEquations.size() - 1).graphColor = mainColorPicker.colorValue;
      }
    }
    for (int i = 0; i < allEquations.size(); i++) {
      if (allEquations.get(i).isFunction) {
        functions.add(allEquations.get(i));
      } else {
        equations.add(allEquations.get(i));
      }
    }
    if (equations.size() > 0) {
      Image equationRender = equationRenderer.drawEquations(equations);
      if (equationRenderer.doImageWriting) {
        TwoDVec<Double> imagePos = new TwoDVec<Double>(-graphViewPane.getPrefWidth() + funcDrawer.midpoint.x, -graphViewPane.getPrefHeight() + funcDrawer.midpoint.y);
        TwoDVec<Double> tempImageSize = new TwoDVec<Double>(graphViewPane.getPrefWidth() * 2 / (funcDrawer.zoom.x * 50), graphViewPane.getPrefHeight() * 2 / (funcDrawer.zoom.y * 50));
        TwoDVec<Double> tempImageZoomOffset = new TwoDVec<Double>(-(tempImageSize.x / 2 - graphViewPane.getPrefWidth()),-(tempImageSize.y / 2 - graphViewPane.getPrefHeight()));
        mainCanvas.getGraphicsContext2D().drawImage(equationRender, imagePos.x + tempImageZoomOffset.x, imagePos.y + tempImageZoomOffset.y, tempImageSize.x, tempImageSize.y);
      }
    }
    funcDrawer.drawFunctions(mainCanvas.getGraphicsContext2D(), functions);
    if (previewEquation != null) {
      funcDrawer.drawFunction(mainCanvas.getGraphicsContext2D(), funcDrawer.getXArray(),
              funcDrawer.calculateFunctionValues(previewEquation), mainColorPicker.colorValue, previewEquation);
    }
    long endTime = System.nanoTime();
    long totalTime = endTime - startTime;
    // System.out.println(totalTime);
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

class Anchor {
  private Region baseObject;
  private Region relateToObject;
  public TwoDVec<Double> offsetVec;
  private boolean keepX = false;
  private boolean keepY = false;
  private String type;
  private TwoDVec<Double> maxSize = new TwoDVec<Double>(10000.0,100000.0);

  public Anchor(Region baseObject, Region relateToObject, TwoDVec<Double> offsetVec, String type) {
    this.baseObject = baseObject;
    this.relateToObject = relateToObject;
    this.offsetVec = offsetVec;
    this.type = type;
  }

  public Anchor(Region baseObject, Region relateToObject, TwoDVec<Double> offsetVec, String type, boolean keepX, boolean keepY) {
    this.baseObject = baseObject;
    this.relateToObject = relateToObject;
    this.offsetVec = offsetVec;
    this.keepX = keepX;
    this.keepY = keepY;
    this.type = type;
  }

  public Anchor(Region baseObject, Region relateToObject, TwoDVec<Double> offsetVec, String type, boolean keepX, boolean keepY, TwoDVec<Double> maxSize) {
    this.baseObject = baseObject;
    this.relateToObject = relateToObject;
    this.offsetVec = offsetVec;
    this.keepX = keepX;
    this.keepY = keepY;
    this.type = type;
    this.maxSize = maxSize;
  }


  public void applyAnchor() {
    if (type.equals("scale")) {
      TwoDVec<Double> targetSize = new TwoDVec<Double>(relateToObject.getPrefWidth() + offsetVec.x,relateToObject.getPrefHeight() + offsetVec.y);
      if (!keepX) {
        if ((targetSize.x <= maxSize.x)) {
          baseObject.setPrefWidth(targetSize.x);
        } else if(baseObject.getPrefWidth() < maxSize.x) {
          baseObject.setPrefWidth(maxSize.x);
        }
      }
      if (!keepY) {
        if (targetSize.y < maxSize.y) {
          baseObject.setPrefHeight(targetSize.y);
        } else if(baseObject.getPrefHeight() < maxSize.y) {
          baseObject.setPrefHeight(maxSize.y);
        }
      }
    }
    if (type.equals("pos")) {
      if (!keepX) {
        baseObject.setLayoutX(relateToObject.getLayoutX() + offsetVec.x);
      }
      if (!keepY) {
        baseObject.setLayoutY(relateToObject.getLayoutY() + offsetVec.y);
      }
    }
    if (type.equals("scale->pos")) {
      if (!keepX) {
        baseObject.setLayoutX(relateToObject.getPrefWidth() + offsetVec.x);
      }
      if (!keepY) {
        baseObject.setLayoutY(relateToObject.getPrefHeight() + offsetVec.y);
      }
    }
    if (type.equals("pos->scale")) {
      TwoDVec<Double> targetSize = new TwoDVec<Double>(relateToObject.getLayoutX() + offsetVec.x,relateToObject.getLayoutY() + offsetVec.y);
      if (!keepX) {
        if (targetSize.x <= maxSize.x) {
          baseObject.setPrefWidth(targetSize.x + offsetVec.x);
        } else if(baseObject.getPrefWidth() < maxSize.x) {
          baseObject.setPrefWidth(maxSize.x);
        }
      }
      if (!keepY) {
        if (targetSize.y <= maxSize.y) {
          baseObject.setPrefHeight(targetSize.y);
        } else if(baseObject.getPrefHeight() < maxSize.y) {
          baseObject.setPrefWidth(maxSize.y);
        }
      }
    }
  }

  public static void applyAnchors(ArrayList<Anchor> anchors) {
    for (int i = 0; i < anchors.size(); i++) {
      anchors.get(i).applyAnchor();
    }
  }

  public static Anchor findAnchorOfObject(Node object, String type, ArrayList<Anchor> anchors) {
    for (int i = 0; i < anchors.size(); i++) {
      if (anchors.get(i).baseObject == object && anchors.get(i).type.equals(type)) {
        return anchors.get(i);
      }
    }
    return null;
  }
}