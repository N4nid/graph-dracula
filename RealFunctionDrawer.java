import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RealFunctionDrawer{
  public double[] calculateFunctionValues(TwoDVec<Integer> resolution, TwoDVec<Double> zoom, TwoDVec<Double> midpoint, EquationTree equation) {
    double offsetX = ((double)resolution.x / (double)2);
    double offsetY = ((double)resolution.y / (double)2);
    double[] returnValues = new double[resolution.x / 2];
    for (int i = 0; i < (resolution.x / 2) + 1; i++) {
      double yValue = equation.calculate(pixelXtoRealX(i * 2, midpoint.x,offsetX,zoom.x),0,null);
      double yCoord = realYToPixelY(yValue, midpoint.y,offsetY, zoom.y);
    }
    return returnValues;
  }

  public void drawFunction(GraphicsContext gc, double[] functionValues, Color color) {
    gc.beginPath();
    gc.moveTo(0,functionValues[0]);
    gc.setStroke(color);
    for (int i = 1; i < functionValues.length; i++) {
      gc.lineTo(i*2,functionValues[i]);
    }
    gc.closePath();
  }

  private static double pixelXtoRealX(int pixelX, double midX, double offsetX, double zoomX) {
    return (pixelX - offsetX) * zoomX - midX;
  }
  private static double realYToPixelY(double realY, double midY, double offsetY, double zoomY) {
    return (- realY - midY) / zoomY + offsetY;
  }
}