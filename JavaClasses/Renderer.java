import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;

public class Renderer {
  public Canvas mainCanvas;
  
  public RenderValues renderValues;
  private ApplicationController controller;
  CoordinateSystemRenderer coordinateSystemRenderer;
  private FunctionRenderer funcDrawer;
  private EquationRenderer equationRenderer;
  private ParametricsRenderer parametricsRenderer;
  
  ArrayList<EquationTree> allEquations = new ArrayList<EquationTree>();
  ArrayList<EquationTree> equations = new ArrayList<EquationTree>();
  ArrayList<EquationTree> functions = new ArrayList<EquationTree>();
  ArrayList<EquationTree> parametrics = new ArrayList<EquationTree>();
  
  public Renderer(ApplicationController controller) {
    mainCanvas = new Canvas();
    this.controller = controller;
    renderValues = new RenderValues(new TwoDVec<Integer>(1050, 573),new TwoDVec<Double>(0.02, 0.02),new TwoDVec<Double>((double)(1050/2),(double)(573/2)));
    coordinateSystemRenderer = new CoordinateSystemRenderer(this);
    funcDrawer = new FunctionRenderer(renderValues);
    equationRenderer = new EquationRenderer(renderValues);
    parametricsRenderer = new ParametricsRenderer(renderValues);
  }
  
  public void refreshEquationRenderer() {
    equationRenderer.lastZoom = new TwoDVec<>(-1.0,-1.0);
  }
  
  public static EquationTree buildTestParametricFlower() {       //return x=4*Math.cos(t)*Math.sin(4*t)      //return y=4*Math.sin(t)*Math.sin(4*t)
    EquationNode root = new EquationNode((byte) 4, "");
    root.left = new EquationNode((byte) 2, "*");
    root.left.left = new EquationNode((byte) 2, "*");
    root.left.right = new EquationNode((byte) 3, "sin");
    root.left.left.left = new EquationNode((byte) 0, "4");
    root.left.left.right = new EquationNode((byte) 3, "cos");
    root.left.right.right = new EquationNode((byte) 2, "*");
    root.left.right.right.left = new EquationNode((byte) 0, "4");
    root.left.right.right.right = new EquationNode((byte) 1, "t");
    root.left.left.right.right = new EquationNode((byte) 1, "t");
    root.right = new EquationNode((byte) 2, "*");
    root.right.left = new EquationNode((byte) 2, "*");
    root.right.right = new EquationNode((byte) 3, "sin");
    root.right.left.left = new EquationNode((byte) 0, "4");
    root.right.left.right = new EquationNode((byte) 3, "sin");
    root.right.right.right = new EquationNode((byte) 2, "*");
    root.right.right.right.left = new EquationNode((byte) 0, "4");
    root.right.right.right.right = new EquationNode((byte) 1, "t");
    root.right.left.right.right = new EquationNode((byte) 1, "t");
    return new EquationTree(root);
  }
  
  public void testParametrics() {
    parametrics.add(buildTestParametricFlower());
    ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> parametricsLines = parametricsRenderer.calculateParametricsLinePoints(parametrics);
    for (int i = 0; i < parametricsLines.size(); i++) {
      //renderLines(parametrics.get(i).graphColor, parametricsLines.get(i));
      renderLines(Color.RED, parametricsLines.get(i));
    }
  }
  
  public void renderEquations(ArrayList<EquationVisElement> listElements, EquationTree previewEquation) {
    mainCanvas.getGraphicsContext2D().clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
    coordinateSystemRenderer.drawCoordinateSystem();
    orderEquations(listElements,previewEquation);

    Variable[] customVariables = null;
    if (controller.customVarList != null) {
      customVariables = controller.customVarList.getAllCustomVars();
    }
    EquationTree[] existingFunctions = controller.getAllFunctions();
    
    if (equations.size() > 0) {
      ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> equationsLines = equationRenderer.calculateEquationsLinePoints(equations,customVariables,existingFunctions);
      for (int i = 0; i < equationsLines.size(); i++) {
        renderLines(equations.get(i).graphColor, equationsLines.get(i));
      }
      testParametrics();
    }
    
    if (functions.size() > 0) {
      ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> functionsLines = funcDrawer.calculateFunctionsLines(functions,customVariables,existingFunctions);
      for (int i = 0; i < functionsLines.size(); i++) {
        renderLines(functions.get(i).graphColor, functionsLines.get(i));
      }
    }
    
    if (previewEquation != null) {
      previewEquation.graphColor = controller.mainColorPicker.colorValue;
      if (previewEquation.isFunction) {
        renderLines(previewEquation.graphColor,funcDrawer.calculateFunctionLines(previewEquation,customVariables,existingFunctions));
      }
      else {
        renderLines(previewEquation.graphColor,equationRenderer.calculateEquationLinePoints(previewEquation));
      }
    }
  }
  
  private void orderEquations(ArrayList<EquationVisElement> listElements, EquationTree previewEquation) {
    allEquations = new ArrayList<EquationTree>();
    equations = new ArrayList<EquationTree>();
    functions = new ArrayList<EquationTree>();
    for (int i = 0; i < listElements.size(); i++) {
      if (!(previewEquation != null && i == controller.editIndex)) {
        allEquations.add(listElements.get(i).equation);
        allEquations.get(allEquations.size() - 1).graphColor = listElements.get(i).colorPicker.colorValue;
      } else {
        allEquations.add(EquationParser.parseString(controller.equationInput.getText()));
        allEquations.get(allEquations.size() - 1).graphColor = controller.mainColorPicker.colorValue;
      }
    }
    
    for (int i = 0; i < allEquations.size(); i++) {
      if (allEquations.get(i).isFunction) {
        functions.add(allEquations.get(i));
      } else {
        equations.add(allEquations.get(i));
      }
    }
  }
  
  public void renderLines(Color graphColor, ArrayList<TwoDVec<TwoDVec<Double>>> lines) {
    mainCanvas.getGraphicsContext2D().setLineWidth(2);
    for (int i = 0; i < lines.size(); i++) {
      mainCanvas.getGraphicsContext2D().setStroke(graphColor);
      TwoDVec<TwoDVec<Double>> currentLine = lines.get(i);
      mainCanvas.getGraphicsContext2D().strokeLine(currentLine.x.x,currentLine.x.y,currentLine.y.x,currentLine.y.y);
    }
  }
  
  public void centerCoordinateSystem() {
    coordinateSystemRenderer.centerCoordinateSystem();
  }
  
}



