import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

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
  
  public void renderEquations(ArrayList<EquationVisElement> listElements) {
    mainCanvas.getGraphicsContext2D().clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
    coordinateSystemRenderer.drawCoordinateSystem();
    orderEquations(listElements);

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
      //testParametrics();
    }
    
    if (functions.size() > 0) {
      long startTime = System.nanoTime();
      ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> functionsLines = funcDrawer.calculateFunctionsLines(functions,customVariables,existingFunctions);
      for (int i = 0; i < functionsLines.size(); i++) {
        renderLines(functions.get(i).graphColor, functionsLines.get(i));
      }
      long endTime = System.nanoTime();
      System.out.println(endTime-startTime);
    }
  }
  
  private void orderEquations(ArrayList<EquationVisElement> listElements) {
    allEquations = new ArrayList<EquationTree>();
    equations = new ArrayList<EquationTree>();
    functions = new ArrayList<EquationTree>();
    parametrics = new ArrayList<EquationTree>();
    for (int i = 0; i < listElements.size(); i++) {
      allEquations.add(listElements.get(i).equation);
      allEquations.get(allEquations.size() - 1).graphColor = listElements.get(i).colorPicker.colorValue;
    }

    for (int i = 0; i < allEquations.size(); i++) {
      if (allEquations.get(i).isFunction) {
        functions.add(allEquations.get(i));
      } else if(allEquations.get(i).isParametric) {
        parametrics.add(allEquations.get(i));
      } else {
        equations.add(allEquations.get(i));
      }
    }
  }
  
  public void renderLines(Color graphColor, ArrayList<TwoDVec<TwoDVec<Double>>> lines) {
    mainCanvas.getGraphicsContext2D().setLineWidth(2);
    correctLines(lines);
    for (int i = 0; i < lines.size(); i++) {
      if (lines.get(i) != null) {
        mainCanvas.getGraphicsContext2D().setStroke(graphColor);
        TwoDVec<TwoDVec<Double>> currentLine = lines.get(i);
        mainCanvas.getGraphicsContext2D().strokeLine(currentLine.x.x, currentLine.x.y, currentLine.y.x, currentLine.y.y);
      }
    }
  }


  private void correctLines(ArrayList<TwoDVec<TwoDVec<Double>>> lines) {
    for (int i = 0; i < lines.size(); i++) {
      double currentSlope = Math.abs(lines.get(i).y.y - lines.get(i).x.y);
      if (currentSlope > renderValues.resolution.y && !yIsOnScreen(lines.get(i).x.y) && !yIsOnScreen(lines.get(i).y.y)) {
        double midpointY = renderValues.midpoint.y;
        if (renderValues.zoom.x < 0.02) {
          midpointY *= (renderValues.zoom.x / 0.02);
        }
        //System.out.println(midpointY);
        //System.out.println(renderValues.screenCoordDoubleToRealCoord(lines.get(i).y).x);
        //System.out.println(currentSlope);
        if (midpointY < 1.7 * renderValues.resolution.y && midpointY > -1.7 * renderValues.resolution.y) { //If you go off too far up or down, the valid slopes will be too steep, and I don't wanna invalidate them
          lines.set(i,null);
        }
      }
    }
  }

  private boolean yIsOnScreen(double y) {
    double minY = -renderValues.midpoint.y - (renderValues.resolution.y / 2);
    double maxY = -renderValues.midpoint.y + (renderValues.resolution.y / 2);
    return (y < maxY && y > minY);
  }

  
  public void centerCoordinateSystem() {
    coordinateSystemRenderer.centerCoordinateSystem();
  }
  
}



