import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CoordinateSystemRenderer {
  
  private RenderValues renderValues;
  private TwoDVec<Integer> axisNumbersDecimalPlaces = new TwoDVec<Integer>(0,0);
  private Font defaultFont;
  private static final double defaultFontSize = 17;
  private GraphicsContext gc;
  private Renderer renderer;
  private double xLabelPadding = 7;
  private double yLabelPadding = 7;
  
  public CoordinateSystemRenderer(Renderer renderer) {
    this.renderer = renderer;
    this.renderValues = renderer.renderValues;
    this.gc = renderer.mainCanvas.getGraphicsContext2D();
    defaultFont = Font.loadFont("file:resources/SourceCodePro-Regular.ttf",defaultFontSize);
  }
  
  public void drawCoordinateSystem() {
    this.gc = renderer.mainCanvas.getGraphicsContext2D();
    double stepSizeX = 1;
    double stepSizeY = 1;
    axisNumbersDecimalPlaces = new TwoDVec<Integer>(0,0);
    stepSizeX = fixStepSize(stepSizeX,true);
    stepSizeY = fixStepSize(stepSizeY,false);
    gc.setStroke(Color.WHITE);
    gc.setLineWidth(1);
    gc.strokeLine(0,renderValues.midpoint.y, renderValues.resolution.x,renderValues.midpoint.y);
    gc.strokeLine(renderValues.midpoint.x,0,renderValues.midpoint.x,renderValues.resolution.y);
    
    gc.setStroke(Effects.changeBrightness(Color.WHITE,0.3));
    gc.setFont(defaultFont);
    drawXCoords(gc,stepSizeX);
    drawYCoords(gc,stepSizeY);
  }
  
  public void centerCoordinateSystem() {
    renderValues.midpoint.setPos((double)(renderValues.resolution.x / 2), (double)(renderValues.resolution.y / 2));
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
  
  private double fixStepSize(double stepSize, boolean isX){
    double targetAxisSteps = (isX)? (renderValues.resolution.x / (double)74) : (renderValues.resolution.y /  (double)74);
    double zoom = (isX) ? renderValues.zoom.x : renderValues.zoom.y;
    double resolution = (isX) ? renderValues.resolution.x : renderValues.resolution.y;
    double realCoordinateResolution = resolution * zoom;
    while (realCoordinateResolution/stepSize < targetAxisSteps) {
      stepSize /= 2;
      if (isX) {
        axisNumbersDecimalPlaces.x += 1;
      }
      else {
        axisNumbersDecimalPlaces.y += 1;
      }
    }
    while (realCoordinateResolution / stepSize > targetAxisSteps*1.5) {
      stepSize*=2;
      if (isX) {
        axisNumbersDecimalPlaces.x -= 1;
        axisNumbersDecimalPlaces.x = Math.max(0,axisNumbersDecimalPlaces.x);
      }
      else {
        axisNumbersDecimalPlaces.y -= 1;
        axisNumbersDecimalPlaces.y = Math.max(0,axisNumbersDecimalPlaces.y);
      }
    }
    return stepSize;
  }
  
  private void drawXCoords(GraphicsContext gc, double stepSizeX) {
    double startNumb = Math.round((- renderValues.midpoint.x)* renderValues.zoom.x / stepSizeX) - 2;
    double endNumb = Math.round(startNumb + renderValues.resolution.x* renderValues.zoom.x / stepSizeX)  + 2;
    double currentX = renderValues.realCoordToScreenCoord(new TwoDVec<Double>((double)startNumb*stepSizeX,0.0)).x;
    double iterator = startNumb;
    gc.setFill(Color.WHITE);

    //This code block ensures, that if number Strings are too long, not every step is labeled, so the labels don't overlap
    double pixelStepSizeX = stepSizeX / renderValues.zoom.x;
    String lastLabelString = String.format("%." + axisNumbersDecimalPlaces.x + "f", endNumb*stepSizeX);
    double lastLabelPixelLength = lastLabelString.length() * (defaultFontSize*0.5);
    int displayOnlyNthStep = (int)((lastLabelPixelLength + xLabelPadding)/pixelStepSizeX) + 1;
    int displayTextIterator = (int)(startNumb) % displayOnlyNthStep;

    while (iterator <= endNumb) {
      boolean canDrawText = false;
      if (displayTextIterator >= displayOnlyNthStep) {
        displayTextIterator = 0;
        canDrawText = true;
      }
      if (iterator != 0) {
        gc.strokeLine(currentX, 0, currentX, renderValues.resolution.y);
        if (canDrawText) {
          String labelString = String.format("%." + axisNumbersDecimalPlaces.x + "f", iterator*stepSizeX);
          int stringLenght = labelString.length();
          if (labelString.contains(".")) {
            stringLenght -= 1;
          }
          gc.fillText(labelString, currentX - 0.28 * defaultFontSize * stringLenght, renderValues.midpoint.y + 1.2 * defaultFontSize);
          displayTextIterator = 0;
        }
      }
      currentX += stepSizeX/renderValues.zoom.x;
      iterator++;
      displayTextIterator++;
    }
  }
  
  private void drawYCoords(GraphicsContext gc, double stepSizeY) {
    double startNumb = Math.round((renderValues.midpoint.y-renderValues.resolution.y)* renderValues.zoom.y / stepSizeY) - 1;
    double endNumb = Math.round(startNumb + renderValues.resolution.y* renderValues.zoom.y / stepSizeY)  + 1;
    //System.out.println(startNumb);
    //System.out.println(endNumb);
    double currentY = renderValues.realCoordToScreenCoord(new TwoDVec<Double>(0.0,(double)startNumb*stepSizeY)).y;
    double iterator = startNumb;
    gc.setFill(Color.WHITE);
    while (iterator <= endNumb) {
      if (iterator != 0) {
        gc.strokeLine(0, currentY, renderValues.resolution.x, currentY);
        
        String labelString = String.format("%." + axisNumbersDecimalPlaces.y + "f", iterator*stepSizeY);
        int stringLenght = labelString.length();
        gc.fillText(labelString, renderValues.midpoint.x - stringLenght * (defaultFontSize*0.5) - yLabelPadding, currentY + 0.3 * defaultFontSize);
      }
      currentY -= stepSizeY/renderValues.zoom.y;
      iterator++;
    }
  }
}