import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class FunctionRenderer {
  RenderValues renderValues;

  private static final double slopeBreakThreshhold = 400;

  private ArrayList<RenderBreakpoints> equationBreakpoints = new ArrayList<RenderBreakpoints>();

  public FunctionRenderer(RenderValues renderValues) {
    this.renderValues = renderValues;
  }

  public double[] calculateFunctionValues(EquationTree equation, Variable[] existingVariables, EquationTree[] existingFunctions) {
    double[] returnValues = new double[renderValues.resolution.x];
    for (int i = 0; i < renderValues.resolution.x; i++) {
      double yValue = equation.calculate(new TwoDVec<Double>(renderValues.screenCoordToRealCoord(new TwoDVec<Integer>(i,0)).x,0.0),existingVariables,existingFunctions);
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

  public ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> calculateFunctionsLines(ArrayList<EquationTree> functions, Variable[] existingVariables, EquationTree[] existingFunctions) {
    ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> functionsLines = new ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>>();
    double[] xValues = getXArray(); 
    for (int i = 0; i < functions.size(); i++) {
      ArrayList<TwoDVec<TwoDVec<Double>>> currentFunctionLines = new ArrayList<TwoDVec<TwoDVec<Double>>>();
      double[] functionValues = calculateFunctionValues(functions.get(i),existingVariables,existingFunctions);
      fixValues(functionValues, functions.get(i));
      for (int j = 0; j < functionValues.length-1; j++) {
        TwoDVec<Double> fromCoord = new TwoDVec<Double>(xValues[j],functionValues[j]);
        TwoDVec<Double> toCoord = new TwoDVec<Double>(xValues[j+1],functionValues[j+1]);
        currentFunctionLines.add(new TwoDVec<TwoDVec<Double>>(fromCoord,toCoord));
      }
      functionsLines.add(currentFunctionLines);
    }
    return functionsLines;
  }


  public ArrayList<TwoDVec<TwoDVec<Double>>> calculateFunctionLines(EquationTree function, Variable[] existingVariables, EquationTree[] existingFunctions) {
    double[] xValues = getXArray();
    ArrayList<TwoDVec<TwoDVec<Double>>> currentFunctionLines = new ArrayList<TwoDVec<TwoDVec<Double>>>();
    double[] functionValues = calculateFunctionValues(function,existingVariables,existingFunctions);
    fixValues(functionValues, function);
    for (int j = 0; j < functionValues.length-1; j++) {
      TwoDVec<Double> fromCoord = new TwoDVec<Double>(xValues[j],functionValues[j]);
      TwoDVec<Double> toCoord = new TwoDVec<Double>(xValues[j+1],functionValues[j+1]);
      currentFunctionLines.add(new TwoDVec<TwoDVec<Double>>(fromCoord,toCoord));
    }
    return currentFunctionLines;
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