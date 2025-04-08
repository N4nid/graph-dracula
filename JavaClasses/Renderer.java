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
  
  ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> functionsLines;
  ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> equationLines;
  ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> parametricsLines;
  
  public Renderer(ApplicationController controller) {
    mainCanvas = new Canvas();
    this.controller = controller;
    renderValues = new RenderValues(new TwoDVec<Integer>(1050, 573),new TwoDVec<Double>(0.02, 0.02),new TwoDVec<Double>((double)(1050/2),(double)(573/2)));
    coordinateSystemRenderer = new CoordinateSystemRenderer(this);
    funcDrawer = new FunctionRenderer(renderValues);
    equationRenderer = new EquationRenderer(renderValues,this);
    parametricsRenderer = new ParametricsRenderer(renderValues,controller);
  }
  
  public static EquationTree buildTestParametricFlower() {       //return x=4*Math.cos(t)*Math.sin(4*t)      //return y=4*Math.sin(t)*Math.sin(4*t)
    EquationNode root = new EquationNode((byte) 4, "");
    root.left = new EquationNode((byte) 2, "*");
    root.left.left = new EquationNode((byte) 2, "*");
    root.left.right = new EquationNode((byte) 3, "sin");
    root.left.left.left = new EquationNode((byte) 0, 4.0);
    root.left.left.right = new EquationNode((byte) 3, "cos");
    root.left.right.right = new EquationNode((byte) 2, "*");
    root.left.right.right.left = new EquationNode((byte) 0, 4.0);
    root.left.right.right.right = new EquationNode((byte) 1, "t");
    root.left.left.right.right = new EquationNode((byte) 1, "t");
    root.right = new EquationNode((byte) 2, "*");
    root.right.left = new EquationNode((byte) 2, "*");
    root.right.right = new EquationNode((byte) 3, "sin");
    root.right.left.left = new EquationNode((byte) 0, 4.0);
    root.right.left.right = new EquationNode((byte) 3, "sin");
    root.right.right.right = new EquationNode((byte) 2, "*");
    root.right.right.right.left = new EquationNode((byte) 0, 4.0);
    root.right.right.right.right = new EquationNode((byte) 1, "t");
    root.right.left.right.right = new EquationNode((byte) 1, "t");
    return new EquationTree(root);
  }
  
  /*public void testParametrics() {
  parametrics.add(buildTestParametricFlower());
  ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> parametricsLines = parametricsRenderer.calculateParametricsLinePoints(parametrics,customVariables,existingFunctions);
  for (int i = 0; i < parametricsLines.size(); i++) {
  //renderLines(parametrics.get(i).graphColor, parametricsLines.get(i));
  renderLines(Color.RED, parametricsLines.get(i));
  }
  }    */
  
  public void renderEquations(ArrayList<EquationVisElement> listElements) {
    mainCanvas.getGraphicsContext2D().clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
    coordinateSystemRenderer.drawCoordinateSystem();
    orderEquations(listElements);
    
    Variable[] customVariables = null;
    if (controller.customVarList != null) {
      customVariables = controller.customVarList.getAllCustomVars();
    }
    EquationTree[] existingFunctions = controller.getAllFunctions();  
    
    if (functions.size() > 0) {
      functionsLines = funcDrawer.calculateFunctionsLines(functions,customVariables,existingFunctions);
      for (int i = 0; i < functionsLines.size(); i++) {
        renderLines(functions.get(i).graphColor, functionsLines.get(i));
      }
    } 
    
    if (equations.size() > 0) {
      equationLines = equationRenderer.calculateEquationsLinePoints(equations,customVariables,existingFunctions);
      for (int i = 0; i < equationLines.size(); i++) {
        renderLines(equations.get(i).graphColor, equationLines.get(i));
      }
    }        
    
    if (parametrics.size() > 0) {
      parametricsLines = parametricsRenderer.calculateParametricsLinePoints(parametrics,customVariables,existingFunctions);
      for (int i = 0; i < parametricsLines.size(); i++) {
        if (parametrics.get(i).rangeCondition != null) {
          fixLinesRange(parametricsLines.get(i),functions.get(i).rangeCondition,customVariables,existingFunctions);
        }
        renderLines(parametrics.get(i).graphColor, parametricsLines.get(i));
      }
    } // end of if
  }
  
  public void rerender() {
    mainCanvas.getGraphicsContext2D().clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
    coordinateSystemRenderer.drawCoordinateSystem();
    if (functions.size() > 0) {
      for (int i = 0; i < functionsLines.size(); i++) {
        if (functions.get(i).rangeCondition != null) {
          fixLinesRange(functionsLines.get(i),functions.get(i).rangeCondition,customVariables,existingFunctions);
        }
        renderLines(functions.get(i).graphColor, functionsLines.get(i));
      }
      long endTime = System.nanoTime();
      //System.out.println(endTime-startTime);
    }
  }

  private void fixLinesRange(ArrayList<TwoDVec<TwoDVec<Double>>> lines, ConditionTree rangeCondition, Variable[] customVariables, EquationTree[] existingFunctions) {
    for (int i = 0; i < lines.size(); i++) {
      TwoDVec<Double> firstCoordinate = renderValues.screenCoordDoubleToRealCoord(lines.get(i).x);
      TwoDVec<Double> secondCoordinate = renderValues.screenCoordDoubleToRealCoord(lines.get(i).y);
      if (!rangeCondition.checkCondition(firstCoordinate,customVariables,existingFunctions) || !rangeCondition.checkCondition(secondCoordinate,customVariables,existingFunctions)) {
        //lines.get(i).x.printDouble();
        lines.get(i).x.x = Double.NaN; //Lines with one NaN coordinate don't get drawn
        lines.get(i).y.x = Double.NaN;
      }
    }
    if (equations.size() > 0) {
      for (int i = 0; i < equationLines.size(); i++) {
        renderLines(equations.get(i).graphColor, equationLines.get(i));
      }
    }
    if (parametrics.size() > 0) {
      for (int i = 0; i < parametricsLines.size(); i++) {
        renderLines(parametrics.get(i).graphColor, parametricsLines.get(i));
      }
    } // end of if
  }
  
  private void orderEquations(ArrayList<EquationVisElement> listElements) {
    allEquations = new ArrayList<EquationTree>();
    equations = new ArrayList<EquationTree>();
    functions = new ArrayList<EquationTree>();
    parametrics = new ArrayList<EquationTree>();
    for (int i = 0; i < listElements.size(); i++) {
      if (listElements.get(i).equation.isVisible) {
        allEquations.add(listElements.get(i).equation);
        allEquations.get(allEquations.size() - 1).graphColor = listElements.get(i).colorPicker.colorValue;
      }
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

  
  private boolean checkLineValidity(TwoDVec<TwoDVec<Double>> prevLine,TwoDVec<TwoDVec<Double>> currentLine, TwoDVec<TwoDVec<Double>> nextLine) {
    double slope = Math.abs(currentLine.x.y - currentLine.y.y);
    if (slope > 3 || Double.isNaN(slope) || Double.isInfinite(slope)) {
      //if (slope>600){return false;}
      double prevSlope = prevLine.y.y - prevLine.x.y;
      double nextSlope = nextLine.y.y - nextLine.x.y;
      if (prevSlope > 0 && nextSlope < 0) {return false;}
      if (prevSlope < 0 && nextSlope > 0) {return false;}
      //System.out.println(currentLine.x.x + " : " + slope + " , " + prevSlope + " , " + nextSlope);
    }
    return true;
  }
  
  public void centerCoordinateSystem() {
    coordinateSystemRenderer.centerCoordinateSystem();
  }
  
}



