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

    public CoordinateSystemRenderer(Renderer renderer) {
        this.renderer = renderer;
        this.renderValues = renderer.renderValues;
        this.gc = renderer.mainCanvas.getGraphicsContext2D();
        defaultFont = Font.loadFont("file:resources/SourceCodePro-Regular.ttf",defaultFontSize);
    }

    public void drawCoordinateSystem() {
        this.gc = renderer.mainCanvas.getGraphicsContext2D();
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
}