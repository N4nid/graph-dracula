import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class RealFunctionDrawer{
  public TwoDVec<Integer> resolution;
  public TwoDVec<Double> zoom;
  public TwoDVec<Double> midpoint;

  private Font defaultFont;
  private static final double defaultFontSize = 17;

  public RealFunctionDrawer(TwoDVec<Integer> resolution, TwoDVec<Double> zoom, TwoDVec<Double> midpoint) {
    this.resolution = resolution;
    this.zoom = zoom;
    this.midpoint = midpoint;
    defaultFont = Font.loadFont("file:resources/SourceCodePro-Regular.ttf",defaultFontSize);
  }

  public double[] calculateFunctionValues(EquationTree equation) {
    double offsetX = ((double)resolution.x / (double)2);
    double offsetY = ((double)resolution.y / (double)2);
    double[] returnValues = new double[resolution.x];
    for (int i = 0; i < resolution.x; i++) {
      double yValue = equation.calculate(pixelXtoRealX(i, midpoint.x,offsetX,zoom.x),0,null);
      returnValues[i] = realYToPixelY(yValue, midpoint.y,offsetY, zoom.y);
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

  public void drawFunctions(GraphicsContext gc, Color[] colors, EquationTree[] functions) {
    gc.clearRect(0,0,gc.getCanvas().getWidth(),gc.getCanvas().getHeight());
    drawCoordinateSystem(gc);
    double[] xValues = getXArray(); 
    for (int i = 0; i < functions.length; i++) {
      double[] functionValues = calculateFunctionValues(functions[i]);
      drawFunction(gc,xValues,functionValues,colors[i]);
    }
  }

  public void drawFunction(GraphicsContext gc, double[] xValues, double[] functionValues, Color color) {
    gc.setStroke(color);
    fixNans(functionValues);
    gc.strokePolyline(xValues,functionValues, xValues.length);
  }

  private void fixNans(double[] fixableValues) {
    for (int i = 2; i < fixableValues.length - 2; i++) {
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
          fixableValues[i] = -5;
        }
        else {
          fixableValues[i] = resolution.y;
        }
        i+=2;
      }
    }
  }
  
  public void drawCoordinateSystem(GraphicsContext gc) {
    double unitDistanceX = 1 / zoom.x;
    double unitDistanceY = 1 / zoom.y;
    TwoDVec<Double> midpointPixelCoord = realCoordToPixel(new TwoDVec<Double>(0.0,0.0));

    gc.setStroke(Color.WHITE);
    gc.strokeLine(0,midpointPixelCoord.y, resolution.x,midpointPixelCoord.y);
    gc.strokeLine(midpointPixelCoord.x,0,midpointPixelCoord.x,resolution.y);

    gc.setStroke(Effects.changeBrightness(Color.WHITE,0.3));
    gc.setFont(defaultFont);
    drawXCoords(gc,unitDistanceX,midpointPixelCoord);
    drawYCoords(gc,unitDistanceY,midpointPixelCoord);
    System.out.println(gc.getFont().getFamily());
  }

  private void drawXCoords(GraphicsContext gc, double unitDistanceX, TwoDVec<Double> midpointPixelCoord) {
    double currentX = midpointPixelCoord.x + unitDistanceX;
    int iterator = 1;
    double mirroredX;
    gc.setFill(Color.WHITE);

    while (currentX <= resolution.x) {
      gc.strokeLine(currentX,0,currentX,resolution.y);
      mirroredX = currentX-2*iterator*unitDistanceX;
      gc.strokeLine(mirroredX,0,mirroredX,resolution.y);

      int stringLenght = ("" + iterator).length();
      gc.fillText("" + iterator, currentX - 0.3 * defaultFontSize * stringLenght,midpointPixelCoord.y + 1.2 * defaultFontSize);
      gc.fillText("-" + iterator, mirroredX - 0.3 * defaultFontSize * (stringLenght + 1),midpointPixelCoord.y + 1.2 * defaultFontSize);

      currentX += unitDistanceX;
      iterator++;
    }
  }

  private void drawYCoords(GraphicsContext gc, double unitDistanceY, TwoDVec<Double> midpointPixelCoord) {
    double currentY = midpointPixelCoord.y - unitDistanceY;
    int iterator = 1;
    double mirroredY;
    gc.setFill(Color.WHITE);
    while (currentY >= 0) {
      gc.strokeLine(0,currentY,resolution.x,currentY);
      mirroredY = currentY+2*iterator*unitDistanceY;
      gc.strokeLine(0,mirroredY,resolution.x,mirroredY);

      int stringLenght = ("" + iterator).length();
      gc.fillText("" + iterator, midpointPixelCoord.x - (0.2 + stringLenght) * defaultFontSize,currentY + 0.3 * defaultFontSize);
      gc.fillText("-" + iterator, midpointPixelCoord.x - (0.2 + stringLenght + 1) * defaultFontSize,mirroredY + 0.3 * defaultFontSize);

      currentY -= unitDistanceY;
      iterator++;
    }
  }

  private static double pixelXtoRealX(int pixelX, double midX, double offsetX, double zoomX) {
    return (pixelX - offsetX) * zoomX - midX;
  }
  private static double realYToPixelY(double realY, double midY, double offsetY, double zoomY) {
    return (- realY - midY) / zoomY + offsetY;
  }

  public TwoDVec<Double> realCoordToPixel(TwoDVec<Double> realCoord) {
    TwoDVec<Double> pixelCoord = new TwoDVec<Double>((realCoord.x - midpoint.x) / zoom.x + resolution.x / 2, (-realCoord.y - midpoint.y) / zoom.y + resolution.y / 2);
    return pixelCoord;
  }
}