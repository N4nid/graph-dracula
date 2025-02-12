import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RealFunctionDrawer{
  public static double[] calculateFunctionValues(TwoDVec<Integer> resolution, TwoDVec<Double> zoom, TwoDVec<Double> midpoint, EquationTree equation) {
    double offsetX = ((double)resolution.x / (double)2);
    double offsetY = ((double)resolution.y / (double)2);
    double[] returnValues = new double[resolution.x];
    for (int i = 0; i < resolution.x; i++) {
      double yValue = equation.calculate(pixelXtoRealX(i, midpoint.x,offsetX,zoom.x),0,null);
      returnValues[i] = realYToPixelY(yValue, midpoint.y,offsetY, zoom.y);
    }
    return returnValues;
  }

  public static void drawFunctions(GraphicsContext gc, double[] xValues, double[][] functionValues, Color color) {
    gc.clearRect(0,0,gc.getCanvas().getWidth(),gc.getCanvas().getHeight());
    for (int i = 0; i < functionValues.length; i++) {
      drawFunction(gc,xValues,functionValues[i],color);
    }
  }

  public static void drawFunction(GraphicsContext gc, double[] xValues, double[] functionValues, Color color) {
    gc.setStroke(color);
    gc.strokePolyline(xValues,functionValues, xValues.length);
  }

  private static double pixelXtoRealX(int pixelX, double midX, double offsetX, double zoomX) {
    return (pixelX - offsetX) * zoomX - midX;
  }
  private static double realYToPixelY(double realY, double midY, double offsetY, double zoomY) {
    return (- realY - midY) / zoomY + offsetY;
  }
}