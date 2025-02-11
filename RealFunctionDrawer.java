import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
public class RealFunctionDrawer{
  public WritableImage render(TwoDVec<Integer> resolution, TwoDVec<Double> zoom, TwoDVec<Double> midpoint, Color functionColor, EquationTree equation) {
    WritableImage returnImage = new WritableImage(resolution.x,resolution.y);
    double offsetX = ((double)resolution.x / (double)2);
    double offsetY = ((double)resolution.y / (double)2);
    Color[][] bildschirm = new Color[resolution.x][resolution.y];
    for (int i = 0; i < (resolution.x / 2) + 1; i++) {
      double yValue = equation.calculate(pixelXtoRealX(i * 2, midpoint.x,offsetX,zoom.x),0,null);
      double yCoord = realYToPixelY(yValue, midpoint.y,offsetY, zoom.y);
    }
    return returnImage;
  }
  private static double pixelXtoRealX(int pixelX, double midX, double offsetX, double zoomX) {
    return (pixelX - offsetX) * zoomX - midX;
  }
  private static double realYToPixelY(double realY, double midY, double offsetY, double zoomY) {
    return (- realY - midY) / zoomY + offsetY;
  }
  
  private static void line_drawing(int x1, int y1, int x2, int y2, Color[][] bildschirm) {       //Breselham's Algorithm
    int x = x1;
    int y = y1;
    int dx = Math.abs((x2 - x1));
    int dy = Math.abs((y2 - y1));
    int s1 = (int) Math.signum((x2 - x1));
    int s2 = (int) Math.signum((y2 - y1));
    boolean interchange = false;
    if (dy > dx) {
      int zwischenspeicher = dx;
      dx = dy;
      dy = zwischenspeicher;
      interchange = true;
    } else {
      interchange = false;
    } // end of if-else
    int e = 2*dy - dx;
    int a = 2*dy;
    int b = 2*dy - 2*dx;
    for (int i = 1; i < dx; i++) {
      if (e < 0) {
        if (interchange) {
          y += s2;
        } else {
          x += s1;
        } // end of if-else
        e += a;
      } else {
        y += s2;
        x += s1;
        e += b;
      } // end of if-else
      if (x >= 0 && x < bildschirm.length && y >= 0 && y < bildschirm[0].length) {
        bildschirm[x][y] = Color.color(0,0,0);
      } // end of if
    }
  }
}