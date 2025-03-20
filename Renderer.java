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
    private FunctionRenderer funcDrawer;
    private EquationRenderer equationRenderer;
    public Renderer(ApplicationController controller) {
        mainCanvas = new Canvas();
        this.controller = controller;
        renderValues = new RenderValues(new TwoDVec<Integer>(1920, 1080), new TwoDVec<Double>(0.02, 0.02), new TwoDVec<Double>(0.0, 0.0));
        coordinateSystemRenderer = new CoordinateSystemRenderer(this);
        funcDrawer = new FunctionRenderer(renderValues);
        equationRenderer = new EquationRenderer(renderValues);
    }

    public void renderEquations(ArrayList<EquationTree> allEquations) {
        mainCanvas.getGraphicsContext2D().clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        coordinateSystemRenderer.drawCoordinateSystem();

        ArrayList<EquationTree> equations = new ArrayList<EquationTree>();
        ArrayList<EquationTree> functions = new ArrayList<EquationTree>();
        for (int i = 0; i < allEquations.size(); i++) {
            if (allEquations.get(i).isFunction) {
                functions.add(allEquations.get(i));
            } else {
                equations.add(allEquations.get(i));
            }
        }
        if (equations.size() > 0) {
            equationRenderer.calculateLinePoints(equations);
        }

        ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> functionsLines = funcDrawer.calculateFunctionsLines(functions);
        for (int i = 0; i < functionsLines.size(); i++) {
            renderLines(functions.get(i).graphColor, functionsLines.get(i));
        }
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



