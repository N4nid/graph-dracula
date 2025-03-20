import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;

public class FunctionRenderer {
  RenderValues renderValues;
  
  private TwoDVec<Integer> axisNumbersDecimalPlaces = new TwoDVec<Integer>(0,0);

  private Font defaultFont;
  private static final double defaultFontSize = 17;
  private static final double slopeBreakThreshhold = 400;

  private ArrayList<RenderBreakpoints> equationBreakpoints = new ArrayList<RenderBreakpoints>();

  public FunctionRenderer(RenderValues renderValues) {
    this.renderValues = renderValues;
    defaultFont = Font.loadFont("file:resources/SourceCodePro-Regular.ttf",defaultFontSize);
  }

  public double[] calculateFunctionValues(EquationTree equation) {
    double[] returnValues = new double[renderValues.resolution.x];
    for (int i = 0; i < renderValues.resolution.x; i++) {
      double yValue = equation.calculate(new TwoDVec<Double>(pixelXtoRealX(i, renderValues.midpoint.x,renderValues.zoom.x),0.0),null);
      returnValues[i] = realYToPixelY(yValue, renderValues.midpoint.y, renderValues.zoom.y);
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
    drawCoordinateSystem(gc);
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
  
  public void drawCoordinateSystem(GraphicsContext gc) {
    double unitDistanceX = 1 / renderValues.zoom.x;
    double unitDistanceY = 1 / renderValues.zoom.y;
    axisNumbersDecimalPlaces = new TwoDVec<Integer>(0,0);
    unitDistanceX = fixUnitDistance(unitDistanceX, true);
    unitDistanceY = fixUnitDistance(unitDistanceY, false);

    gc.setStroke(Color.WHITE);
    gc.setLineWidth(1);
    gc.strokeLine(0,renderValues.midpoint.y, renderValues.resolution.x,renderValues.midpoint.y);
    gc.strokeLine(renderValues.midpoint.x,0,renderValues.midpoint.x,renderValues.resolution.y);

    gc.setStroke(Effects.changeBrightness(Color.WHITE,0.3));
    gc.setFont(defaultFont);
    drawXCoords(gc,unitDistanceX);
    drawYCoords(gc,unitDistanceY);
  }

  private double fixUnitDistance(double unitDistance, boolean isX){
    double axisRes = 1000;
    while (axisRes / unitDistance < 10) {
      unitDistance /= 2;
      if (isX) {
        axisNumbersDecimalPlaces.x += 1;
      }
      else {
        axisNumbersDecimalPlaces.y += 1;
      }
    }
    while (axisRes / unitDistance > 20) {
      unitDistance *= 2;
    }
    return unitDistance;
  }

  private void drawXCoords(GraphicsContext gc, double unitDistanceX) {
    int startNumb = (int)Math.round((- renderValues.midpoint.x) / unitDistanceX) - 1;
    int endNumb = (int)Math.round(startNumb + renderValues.resolution.x /unitDistanceX)  + 1;
    //System.out.println(startNumb);
    //System.out.println(endNumb);
    int currentX = (int) (startNumb * unitDistanceX + renderValues.midpoint.x);
    int iterator = startNumb;
    gc.setFill(Color.WHITE);

    while (iterator <= endNumb) {
      if (iterator != 0) {
        gc.strokeLine(currentX, 0, currentX, renderValues.resolution.y);

        String labelString = String.format("%." + axisNumbersDecimalPlaces.x + "f", renderValues.screenCoordToRealCoord(new TwoDVec<Integer>(currentX,0)).x);
        int stringLenght = labelString.length();
        gc.fillText(labelString, currentX - 0.3 * defaultFontSize * stringLenght, renderValues.midpoint.y + 1.2 * defaultFontSize);
      }
      currentX += unitDistanceX;
      iterator++;
    }
  }

  private void drawYCoords(GraphicsContext gc, double unitDistanceY) {
    int endNumb = (int)Math.round(( renderValues.midpoint.y) / unitDistanceY) + 2;
    int startNumb = (int) (endNumb - renderValues.resolution.y / unitDistanceY) - 2;
    /*System.out.println(startNumb);
    System.out.println(endNumb);*/
    int currentY = (int) (- startNumb * unitDistanceY + renderValues.midpoint.y);
    int iterator = startNumb;
    gc.setFill(Color.WHITE);
    while (iterator <= endNumb) {
      if (iterator != 0) {
        gc.strokeLine(0, currentY, renderValues.resolution.x, currentY);

        String labelString = String.format("%." + axisNumbersDecimalPlaces.y + "f", renderValues.screenCoordToRealCoord(new TwoDVec<Integer>(0,currentY)).y);
        int stringLenght = labelString.length();
        gc.fillText(labelString, renderValues.midpoint.x - (0.8 * stringLenght) * defaultFontSize, currentY + 0.3 * defaultFontSize);
      }
      currentY -= unitDistanceY;
      iterator++;
    }
  }

  private static double pixelXtoRealX(int pixelX, double midX, double zoomX) {
    return (pixelX - midX) * zoomX;
  }
  private static double realYToPixelY(double realY, double midY, double zoomY) {
    return - realY / zoomY + midY;
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