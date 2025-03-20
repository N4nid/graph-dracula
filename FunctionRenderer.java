import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;

public class FunctionRenderer {
  RenderValues renderValues;
  

  private static final double slopeBreakThreshhold = 400;

  private ArrayList<RenderBreakpoints> equationBreakpoints = new ArrayList<RenderBreakpoints>();

  public FunctionRenderer(RenderValues renderValues) {
    this.renderValues = renderValues;
  }

  public double[] calculateFunctionValues(EquationTree equation) {
    double[] returnValues = new double[renderValues.resolution.x];
    for (int i = 0; i < renderValues.resolution.x; i++) {
      double yValue = equation.calculate(new TwoDVec<Double>(renderValues.screenCoordToRealCoord(new TwoDVec<Integer>(i,0)).x,0.0),null);
      returnValues[i] = renderValues.realCoordToScreenCoord(new TwoDVec<Double>(0.0,yValue)).y;
    }
    return returnValues;
  }

  public double[] getXArray() {
    double[] xs = new double[Math.abs(renderValues.resolution.x)];
    for (int i = 0; i < renderValues.resolution.x; i++) {
      xs[i] = i;
    }
    return xs;
  }

  public void drawFunctions(GraphicsContext gc, ArrayList<EquationTree> functions) {
    System.out.println("real: " + gc);
    double[] xValues = getXArray(); 
    for (int i = 0; i < functions.size(); i++) {
      double[] functionValues = calculateFunctionValues(functions.get(i));
      fixValues(functionValues, functions.get(i));
      drawFunction(gc,xValues,functionValues,functions.get(i).graphColor,functions.get(i));
    }
  }

  public void drawFunction(GraphicsContext gc, double[] xValues, double[] functionValues, Color color,EquationTree function) {
    gc.setStroke(color);
    gc.setLineWidth(2);
    RenderBreakpoints associatedBreakpoints = RenderBreakpoints.findFunctionBreakpoints(equationBreakpoints,function);
    if (associatedBreakpoints!=null){
    }
    gc.strokePolyline(xValues,functionValues, xValues.length);
  }

  private void fixValues(double[] fixableValues, EquationTree fixableFunction) {
    for (int i = 2; i < fixableValues.length - 2; i++) {
      if (!Double.isNaN(fixableValues[i]) && Double.isNaN(fixableValues[i+1])){
        if (Math.abs(fixableValues[i] - fixableValues[i+1]) > slopeBreakThreshhold) {
          RenderBreakpoints associatedBreakpoints = RenderBreakpoints.findFunctionBreakpoints(equationBreakpoints,fixableFunction);
          if (associatedBreakpoints != null) {
            if (!associatedBreakpoints.breakpoints.contains((double)i)) {
              associatedBreakpoints.breakpoints.add((double)i);
            }
          }
          else {
            equationBreakpoints.add(new RenderBreakpoints(fixableFunction,(double)i));
          }
        }
      }
      if (Double.isNaN(fixableValues[i]) && !Double.isNaN(fixableValues[i+1]) && !Double.isNaN(fixableValues[i+2])) {
        if (fixableValues[i + 2] - fixableValues[i + 1] < 0) {
          fixableValues[i] = renderValues.resolution.y;
        }
        else {
          fixableValues[i] = -5;
        }
      }
      else if (Double.isNaN(fixableValues[i]) && !Double.isNaN(fixableValues[i-1]) && !Double.isNaN(fixableValues[i-2])) {
        if (fixableValues[i -1] - fixableValues[i -2] < 0) {
          fixableValues[i] = renderValues.resolution.y;
        }
        else {
          fixableValues[i] = -5;
        }
        i+=2;
      }
    }
  }

}

class RenderBreakpoints {
  public EquationTree brokenFunction;
  public ArrayList<Double> breakpoints;
  public RenderBreakpoints(EquationTree brokenFunction, double initBreakpoint) {
    this.brokenFunction = brokenFunction;
    this.breakpoints = new ArrayList<Double>();
    this.breakpoints.add(initBreakpoint);
  }

  public static RenderBreakpoints findFunctionBreakpoints(ArrayList<RenderBreakpoints> brokenFunctions, EquationTree function){
    for (int i = 0; i < brokenFunctions.size(); i++) {
      if (brokenFunctions.get(i).brokenFunction==function) {
        return brokenFunctions.get(i);
      }
    }
    return null;
  }
}