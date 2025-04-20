import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Renderer {
  public Canvas mainCanvas;
  
  public RenderValues renderValues;
  private ApplicationController controller;
  CoordinateSystemRenderer coordinateSystemRenderer;
  private FunctionRenderer funtionRenderer;
  private EquationRenderer equationRenderer;
  private ParametricsRenderer parametricsRenderer;

  ArrayList<EquationTree> allEquations = new ArrayList<EquationTree>();
  ArrayList<EquationTree> equations = new ArrayList<EquationTree>();
  ArrayList<EquationTree> functions = new ArrayList<EquationTree>();
  ArrayList<EquationTree> parametrics = new ArrayList<EquationTree>();

  private ArrayList<EquationTree> pepperFunctions;
  private boolean renderPepper;
  
  ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> equationLines;
  
  public Renderer(ApplicationController controller) {
    mainCanvas = new Canvas();
    this.controller = controller;
    renderValues = new RenderValues(new TwoDVec<Integer>(1050, 573),new TwoDVec<Double>(0.02, 0.02),new TwoDVec<Double>((double)(1050/2),(double)(573/2)));
    coordinateSystemRenderer = new CoordinateSystemRenderer(this);
    funtionRenderer = new FunctionRenderer(renderValues);
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
    orderEquations(listElements);
    
    Variable[] customVariables = null;
    if (controller.customVarList != null) {
      customVariables = controller.customVarList.getAllCustomVars();
    }
    EquationTree[] existingFunctions = controller.getAllFunctions();
    
    if (equations.size() > 0) {
      equationLines = equationRenderer.calculateEquationsLinePoints(equations,customVariables,existingFunctions);
    }
    
    rerender();
  }
  
  public void rerender() {
    Variable[] customVariables = null;
    if (controller.customVarList != null) {
      customVariables = controller.customVarList.getAllCustomVars();
    }
    EquationTree[] existingFunctions = controller.getAllFunctions();
    mainCanvas.getGraphicsContext2D().clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
    coordinateSystemRenderer.drawCoordinateSystem();
    if (functions.size() > 0) {
      ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> functionsLines = funtionRenderer.calculateFunctionsLines(functions,customVariables,existingFunctions);
      for (int i = 0; i < functionsLines.size(); i++) {  
        if (functions.get(i).rangeCondition != null) {
          fixLinesRange(functionsLines.get(i),functions.get(i).rangeCondition,customVariables,existingFunctions);
        }
        renderLines(functions.get(i).graphColor, functionsLines.get(i));
      }
    }
    
    if (equations.size() > 0) {
      for (int i = 0; i < equationLines.size(); i++) {
        if (i < equations.size()) {
          if (equations.get(i).rangeCondition != null) {
            fixLinesRange(equationLines.get(i),equations.get(i).rangeCondition,customVariables,existingFunctions);
          }
          renderLines(equations.get(i).graphColor, equationLines.get(i));
        } // end of if
      }
    }
    
    if (parametrics.size() > 0) {
      ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> parametricsLines = parametricsRenderer.calculateParametricsLinePoints(parametrics,customVariables,existingFunctions);
      for (int i = 0; i < parametricsLines.size(); i++) {
        if (parametrics.get(i).rangeCondition != null) {
          fixLinesRange(parametricsLines.get(i),parametrics.get(i).rangeCondition,customVariables,existingFunctions);
        }
        renderLines(parametrics.get(i).graphColor, parametricsLines.get(i));
      }
    } // end of if

    if (renderPepper) {
      EquationTree[] pepperFuncArray = pepperFunctions.toArray(new EquationTree[pepperFunctions.size()]);
      ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> pepperLines = funtionRenderer.calculateFunctionsLines(pepperFunctions,null,pepperFuncArray);
      for (int i = 0; i < pepperLines.size(); i++) {
        if (pepperFunctions.get(i).rangeCondition != null) {
          fixLinesRange(pepperLines.get(i),pepperFunctions.get(i).rangeCondition,null,pepperFuncArray);
        }
        renderLines(pepperFunctions.get(i).graphColor, pepperLines.get(i));
      }
    }
  }
  
  private void fixLinesRange(ArrayList<TwoDVec<TwoDVec<Double>>> lines, ConditionTree rangeCondition, Variable[] customVariables, EquationTree[] existingFunctions) {
    for (int i = 0; i < lines.size(); i++) {
      TwoDVec<Double> firstCoordinate = lines.get(i).x;
      TwoDVec<Double> secondCoordinate = lines.get(i).y;
      if (!rangeCondition.checkCondition(firstCoordinate,customVariables,existingFunctions) || !rangeCondition.checkCondition(secondCoordinate,customVariables,existingFunctions)) {
        //lines.get(i).x.printDouble();
        lines.get(i).x.x = Double.NaN; //Lines with one NaN coordinate don't get drawn
        lines.get(i).y.x = Double.NaN;
      }
    }
  }
  
  private void orderEquations(ArrayList<EquationVisElement> listElements) {
    allEquations = new ArrayList<EquationTree>();
    equations = new ArrayList<EquationTree>();
    functions = new ArrayList<EquationTree>();
    parametrics = new ArrayList<EquationTree>();
    for (int i = 0; i < listElements.size(); i++) {
      if (listElements.get(i).equation != null && listElements.get(i).equation.isVisible) {
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
        TwoDVec<TwoDVec<Double>> currentLine = new TwoDVec<TwoDVec<Double>>(renderValues.realCoordToScreenCoord(lines.get(i).x),renderValues.realCoordToScreenCoord(lines.get(i).y));
        mainCanvas.getGraphicsContext2D().strokeLine(currentLine.x.x, currentLine.x.y, currentLine.y.x, currentLine.y.y);
      }
    }
  }
  
  
  private void correctLines(ArrayList<TwoDVec<TwoDVec<Double>>> lines) {
    for (int i = 0; i < lines.size(); i++) {
      double currentSlope = Math.abs(lines.get(i).y.y - lines.get(i).x.y)/renderValues.zoom.y;
      if (currentSlope > renderValues.resolution.y && !yIsOnScreen(lines.get(i).x.y) && !yIsOnScreen(lines.get(i).y.y)) {
        double midpointY = renderValues.midpoint.y;
        if (renderValues.zoom.x < 0.02) {
          midpointY *= (renderValues.zoom.x / 0.02);
        }
        if (midpointY < 1.7 * renderValues.resolution.y && midpointY > -1.7 * renderValues.resolution.y) { //If you go off too far up or down, the valid slopes will be too steep, and I don't wanna invalidate them
          lines.set(i,null);
        }
      }
    }
  }
  
  private boolean yIsOnScreen(double y) {
    y /= renderValues.zoom.y;
    double minY = (-renderValues.midpoint.y - (renderValues.resolution.y / 2));
    double maxY = (-renderValues.midpoint.y + (renderValues.resolution.y / 2));
    return (y < maxY && y > minY);
  }

  
  public void centerCoordinateSystem() {
    coordinateSystemRenderer.centerCoordinateSystem();
  }

  public void flipAutoAdjustLOD() {
    funtionRenderer.autoAdjustLOD = !funtionRenderer.autoAdjustLOD;
  }

  public void setRenderPepper(boolean renderPepper){
    this.renderPepper = renderPepper;
    if (renderPepper && pepperFunctions == null) {
      parsePepper();
    }
  }

  public boolean getRenderPepper() {
    return renderPepper;
  }

  private void parsePepper() {
    //Initialize funtions without conditions
    pepperFunctions = new ArrayList<>();
    pepperFunctions.add(EquationParser.parseString("g(x)=(x/3)^4-2(x/3)^2-5",controller));
    pepperFunctions.add(EquationParser.parseString("h(x)=1.6*sin(0.1x^2)+5",controller));
    pepperFunctions.add(EquationParser.parseString("i(x)=1.9x+7",controller));
    pepperFunctions.add(EquationParser.parseString("j(x)=1.9*x+4.1",controller));
    pepperFunctions.add(EquationParser.parseString("k(x)=(-1/1.9)x+9",controller));

    //Now, I can add the conditions (because they are dependent on the other functions already existing
    pepperFunctions.set(0,EquationParser.parseString("g(x)=(x/3)^4-2(x/3)^2-5 if(y<h(x))",pepperFunctions,null));
    pepperFunctions.set(1,EquationParser.parseString("h(x)=1.6*sin(0.1x^2)+5 if(y>g(x))",pepperFunctions,null));
    pepperFunctions.set(2,EquationParser.parseString("i(x)=1.9x+7 if(y>h(x)&y<k(x))",pepperFunctions,null));
    pepperFunctions.set(3,EquationParser.parseString("j(x)=1.9*x+4.1 if(y>h(x) & y<k(x))",pepperFunctions,null));
    pepperFunctions.set(4,EquationParser.parseString("k(x)=(-1/1.9)x+9 if(y<i(x)&y>j(x))",pepperFunctions,null));

    //setup Colors
    pepperFunctions.get(0).graphColor = RoundColorPicker.getColorFromIndex(1);
    pepperFunctions.get(1).graphColor = RoundColorPicker.getColorFromIndex(1);
    pepperFunctions.get(2).graphColor = RoundColorPicker.getColorFromIndex(8);
    pepperFunctions.get(3).graphColor = RoundColorPicker.getColorFromIndex(8);
    pepperFunctions.get(4).graphColor = RoundColorPicker.getColorFromIndex(8);
  }
  
}



