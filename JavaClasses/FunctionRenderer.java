import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class FunctionRenderer {
  public boolean autoAdjustLOD = true;

  private double pixelsPerPoint;

  RenderValues renderValues;

  private static final double slopeBreakThreshhold = 400;


  public FunctionRenderer(RenderValues renderValues) {
    this.renderValues = renderValues;
  }

  public double[] calculateFunctionValues(EquationTree equation, Variable[] existingVariables, EquationTree[] existingFunctions) {
    double[] returnValues = new double[(int)Math.round((double)renderValues.resolution.x/pixelsPerPoint)];
    for (int i = 0; i < returnValues.length; i++) {
      double yValue = equation.calculate(new TwoDVec<Double>(renderValues.screenCoordDoubleToRealCoord(new TwoDVec<Double>((double)i*pixelsPerPoint,0.0)).x,0.0),existingVariables,existingFunctions);
      returnValues[i] = yValue;
    }
    return returnValues;
  }

  public double[] getXArray() {
    double[] xs = new double[Math.abs((int)Math.round((double)renderValues.resolution.x/pixelsPerPoint))];
    for (int i = 0; i < xs.length; i++) {
      xs[i] = (double) i * pixelsPerPoint;
    }
    return xs;
  }

  public ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> calculateFunctionsLines(ArrayList<EquationTree> functions, Variable[] existingVariables, EquationTree[] existingFunctions) {
    if (renderValues.zoom.x > 0.02) {
      pixelsPerPoint = Math.max(0.01/renderValues.zoom.x,0.2);
    }
    else {
      pixelsPerPoint = 1;
    }
    ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> functionsLines = new ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>>();
    double[] xValues = getXArray(); 
    for (int i = 0; i < functions.size(); i++) {
      ArrayList<TwoDVec<TwoDVec<Double>>> currentFunctionLines = new ArrayList<TwoDVec<TwoDVec<Double>>>();
      double[] functionValues = calculateFunctionValues(functions.get(i),existingVariables,existingFunctions);
      for (int j = 0; j < functionValues.length-1; j++) {
        TwoDVec<Double> fromCoord = new TwoDVec<Double>(renderValues.screenCoordDoubleToRealCoord(new TwoDVec<Double>(xValues[j],0.0)).x,functionValues[j]);
        TwoDVec<Double> toCoord = new TwoDVec<Double>(renderValues.screenCoordDoubleToRealCoord(new TwoDVec<Double>(xValues[j+1],0.0)).x,functionValues[j+1]);
        currentFunctionLines.add(new TwoDVec<TwoDVec<Double>>(fromCoord,toCoord));
      }
      functionsLines.add(currentFunctionLines);
    }
    return functionsLines;
  }

}