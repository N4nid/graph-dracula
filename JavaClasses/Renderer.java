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
      ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> functionsLines = funcDrawer.calculateFunctionsLines(functions,customVariables,existingFunctions);
      for (int i = 0; i < functionsLines.size(); i++) {
        renderLines(functions.get(i).graphColor, functionsLines.get(i));
      }
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
    for (int i = 0; i < lines.size(); i++) {
      if (i == 0 || i == lines.size() - 1 || ((i > 0 && i < lines.size() - 1) && checkLineValidity(lines.get(i-1),lines.get(i),lines.get(i+1)))) {
        mainCanvas.getGraphicsContext2D().setStroke(graphColor);
        TwoDVec<TwoDVec<Double>> currentLine = lines.get(i);
        mainCanvas.getGraphicsContext2D().strokeLine(currentLine.x.x,currentLine.x.y,currentLine.y.x,currentLine.y.y);
      }
    }
  }

  private boolean checkLineValidity(TwoDVec<TwoDVec<Double>> prevLine,TwoDVec<TwoDVec<Double>> currentLine, TwoDVec<TwoDVec<Double>> nextLine) {
    double slope = Math.abs(currentLine.x.y - currentLine.y.y);
    if (slope > 3 || Double.isNaN(slope) || Double.isInfinite(slope)) {
      //if (slope>600){return false;}
      double prevSlope = prevLine.y.y - prevLine.x.y;
      double nextSlope = nextLine.y.y - nextLine.x.y;
      if (prevSlope > 0 && nextSlope < 0) {return false;}
      if (prevSlope < 0 && nextSlope > 0) {return false;}
      System.out.println(currentLine.x.x + " : " + slope + " , " + prevSlope + " , " + nextSlope);
    }
    return true;
  }
  
  public void centerCoordinateSystem() {
    coordinateSystemRenderer.centerCoordinateSystem();
  }
  
}



