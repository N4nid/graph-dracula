import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;

public class RealFunctionDrawer{
  public TwoDVec<Integer> resolution;
  public TwoDVec<Double> zoom;
  public TwoDVec<Double> midpoint;

  private Font defaultFont;
  private static final double defaultFontSize = 17;
  private static final double slopeBreakThreshhold = 400;

  private ArrayList<RenderBreakpoints> equationBreakpoints = new ArrayList<RenderBreakpoints>();

  public RealFunctionDrawer(TwoDVec<Integer> resolution, TwoDVec<Double> zoom, TwoDVec<Double> midpoint) {
    this.resolution = resolution;
    this.zoom = zoom;
    this.midpoint = midpoint;
    defaultFont = Font.loadFont("file:resources/SourceCodePro-Regular.ttf",defaultFontSize);
  }

  public double[] calculateFunctionValues(EquationTree equation) {
    double[] returnValues = new double[resolution.x];
    for (int i = 0; i < resolution.x; i++) {
      double yValue = equation.calculate(pixelXtoRealX(i, midpoint.x,zoom.x),0,null);
      returnValues[i] = realYToPixelY(yValue, midpoint.y, zoom.y);
    }
    return returnValues;
  }

  public double[] getXArray() {
    double[] xs = new double[resolution.x];
    for (int i = 0; i < resolution.x; i++) {
      xs[i] = i;
    }
    return xs;
  }

  public void centerCoordinateSystem() {
    midpoint.setPos((double)(resolution.x / 2), (double)(resolution.y / 2));
  }

  public void drawFunctions(GraphicsContext gc, Color[] colors, EquationTree[] functions) {
    gc.clearRect(0,0,gc.getCanvas().getWidth(),gc.getCanvas().getHeight());
    drawCoordinateSystem(gc);
    double[] xValues = getXArray(); 
    for (int i = 0; i < functions.length; i++) {
      double[] functionValues = calculateFunctionValues(functions[i]);
      fixValues(functionValues, functions[i]);
      drawFunction(gc,xValues,functionValues,colors[i],functions[i]);
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
          fixableValues[i] = resolution.y;
        }
        else {
          fixableValues[i] = -5;
        }
      }
      else if (Double.isNaN(fixableValues[i]) && !Double.isNaN(fixableValues[i-1]) && !Double.isNaN(fixableValues[i-2])) {
        if (fixableValues[i -1] - fixableValues[i -2] < 0) {
          fixableValues[i] = resolution.y;
        }
        else {
          fixableValues[i] = -5;
        }
        i+=2;
      }
    }
  }
  
  public void drawCoordinateSystem(GraphicsContext gc) {
    double unitDistanceX = 1 / zoom.x;
    double unitDistanceY = 1 / zoom.y;

    gc.setStroke(Color.WHITE);
    gc.setLineWidth(1);
    gc.strokeLine(0,midpoint.y, resolution.x,midpoint.y);
    gc.strokeLine(midpoint.x,0,midpoint.x,resolution.y);

    gc.setStroke(Effects.changeBrightness(Color.WHITE,0.3));
    gc.setFont(defaultFont);
    drawXCoords(gc,unitDistanceX);
    drawYCoords(gc,unitDistanceY);
  }

  private void drawXCoords(GraphicsContext gc, double unitDistanceX) {
    int startNumb = (int)Math.round((- midpoint.x) * zoom.x) - 1;
    //System.out.println(startNumb);
    int endNumb = (int)Math.round((- midpoint.x + resolution.x) * zoom.x)  + 1;
    //System.out.println(endNumb);
    double currentX = startNumb * unitDistanceX + midpoint.x;
    int iterator = startNumb;
    gc.setFill(Color.WHITE);

    while (iterator <= endNumb) {
      if (iterator != 0) {
        gc.strokeLine(currentX, 0, currentX, resolution.y);

        int stringLenght = ("" + iterator).length();
        gc.fillText("" + iterator, currentX - 0.3 * defaultFontSize * stringLenght, midpoint.y + 1.2 * defaultFontSize);
      }
      currentX += unitDistanceX;
      iterator++;
    }
  }

  private void drawYCoords(GraphicsContext gc, double unitDistanceY) {
    int startNumb = (int)Math.round((midpoint.y - resolution.y) * zoom.y) - 1;
    int endNumb = (int)Math.round(midpoint.y * zoom.y) + 1;
    double currentY = - startNumb * unitDistanceY + midpoint.y;
    int iterator = startNumb;
    gc.setFill(Color.WHITE);
    while (iterator <= endNumb) {
      if (iterator != 0) {
        gc.strokeLine(0, currentY, resolution.x, currentY);

        int stringLenght = ("" + iterator).length();
        gc.fillText("" + iterator, midpoint.x - (0.2 + stringLenght) * defaultFontSize, currentY + 0.3 * defaultFontSize);
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

  public TwoDVec<Double> realCoordToPixel(TwoDVec<Double> realCoord) {
    TwoDVec<Double> pixelCoord = new TwoDVec<Double>(realCoord.x / zoom.x +  midpoint.x, (realCoord.y) / -zoom.y + midpoint.y);
    return pixelCoord;
  }

  public TwoDVec<Double> pixelCordToReal(TwoDVec<Double> pixelCoord) {
    TwoDVec<Double> realCord = new TwoDVec<Double>((pixelCoord.x - midpoint.x)* zoom.x, (-pixelCoord.y- midpoint.y) * zoom.y);
    return realCord;
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