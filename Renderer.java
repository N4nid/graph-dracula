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

    ArrayList<EquationTree> allEquations = new ArrayList<EquationTree>();
    ArrayList<EquationTree> equations = new ArrayList<EquationTree>();
    ArrayList<EquationTree> functions = new ArrayList<EquationTree>();

    public Renderer(ApplicationController controller) {
        mainCanvas = new Canvas();
        this.controller = controller;
        renderValues = new RenderValues(new TwoDVec<Integer>(1920, 1080), new TwoDVec<Double>(0.02, 0.02), new TwoDVec<Double>(0.0, 0.0));
        coordinateSystemRenderer = new CoordinateSystemRenderer(this);
        funcDrawer = new FunctionRenderer(renderValues);
        equationRenderer = new EquationRenderer(renderValues);
    }

    public void refreshEquationRenderer() {
        equationRenderer.lastZoom = new TwoDVec<>(-1.0,-1.0);
    }

    public void renderEquations(ArrayList<EquationVisElement> listElements, EquationTree previewEquation) {
        mainCanvas.getGraphicsContext2D().clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        coordinateSystemRenderer.drawCoordinateSystem();
        orderEquations(listElements,previewEquation);

        if (equations.size() > 0) {
            ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> equationsLines = equationRenderer.calculateEquationsLinePoints(equations);
            for (int i = 0; i < equationsLines.size(); i++) {
                renderLines(equations.get(i).graphColor, equationsLines.get(i));
            }
        }

        ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> functionsLines = funcDrawer.calculateFunctionsLines(functions);
        for (int i = 0; i < functionsLines.size(); i++) {
            renderLines(functions.get(i).graphColor, functionsLines.get(i));
        }

        if (previewEquation != null) {
            previewEquation.graphColor = controller.mainColorPicker.colorValue;
            if (previewEquation.isFunction) {
                renderLines(previewEquation.graphColor,funcDrawer.calculateFunctionLines(previewEquation));
            }
            else {
                renderLines(previewEquation.graphColor,equationRenderer.calculateEquationLinePoints(previewEquation));
            }
        }
    }

    private void orderEquations(ArrayList<EquationVisElement> listElements, EquationTree previewEquation) {
        allEquations = new ArrayList<EquationTree>();
        equations = new ArrayList<EquationTree>();
        functions = new ArrayList<EquationTree>();
        for (int i = 0; i < listElements.size(); i++) {
            if (!(previewEquation != null && i == controller.editIndex)) {
                allEquations.add(listElements.get(i).equation);
                allEquations.get(allEquations.size() - 1).graphColor = listElements.get(i).colorPicker.colorValue;
            } else {
                allEquations.add(EquationParser.parseString(controller.equationInput.getText()));
                allEquations.get(allEquations.size() - 1).graphColor = controller.mainColorPicker.colorValue;
            }
        }

        for (int i = 0; i < allEquations.size(); i++) {
            if (allEquations.get(i).isFunction) {
                functions.add(allEquations.get(i));
            } else {
                equations.add(allEquations.get(i));
            }
        }
    }

    public void renderLines(Color graphColor, ArrayList<TwoDVec<TwoDVec<Double>>> lines) {
        mainCanvas.getGraphicsContext2D().setLineWidth(2);
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



