import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;

public class Renderer {
    public Canvas mainCanvas;

    public RenderValues renderValues;
    private ApplicationController controller;
    CoordinateSystemRenderer coordinateSystemRenderer;
    public Renderer(ApplicationController controller) {
        mainCanvas = new Canvas();
        this.controller = controller;
        renderValues = new RenderValues(new TwoDVec<Integer>(1920, 1080), new TwoDVec<Double>(0.02, 0.02), new TwoDVec<Double>(0.0, 0.0));
        coordinateSystemRenderer = new CoordinateSystemRenderer(this);
    }

    public void renderEquations(ArrayList<EquationTree> equations) {
        mainCanvas.getGraphicsContext2D().clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        coordinateSystemRenderer.drawCoordinateSystem();
    }

    public void renderLines(Color graphColor, ArrayList<TwoDVec<TwoDVec<Double>>> lines) {
        for (int i = 0; i < lines.size(); i++) {
            mainCanvas.getGraphicsContext2D().setStroke(graphColor);
            TwoDVec<TwoDVec<Double>> currentLine = lines.get(i);
            mainCanvas.getGraphicsContext2D().strokeLine(currentLine.x.x,currentLine.x.y,currentLine.y.x,currentLine.y.y);
        }
    }

    public void centerCoordinateSystem() {
        coordinateSystemRenderer.centerCoordinateSystem();
    }

}



