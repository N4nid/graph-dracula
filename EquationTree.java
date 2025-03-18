import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class EquationTree{

    public EquationNode root;
    public Color graphColor = Color.BLACK;
    public boolean isFunction = false;
    public Canvas renderCanvas;

    public EquationTree(byte rootState, String rootValue) {
        this.root = new EquationNode(rootState,rootValue);
    }

    public EquationTree(byte rootState, double rootValue) {
        this.root = new EquationNode(rootState,rootValue);
    }

    public EquationTree(EquationNode root) {
        this.root = root;
    }

    public EquationTree() {}

    public double calculate(double x, double y, Variable[] parameters) {
        return root.calculate(x,y,parameters);
    }

    public void renderEquation(ArrayList<ArrayList<TwoDVec<Double>>> linePoints, Color graphColor) {
        for (int i = 0; i < linePoints.size(); i++) {
            ArrayList<TwoDVec<Double>> currentConnectedLine = linePoints.get(i);
            double[] xValues = new double[currentConnectedLine.size()];
            double[] yValues = new double[currentConnectedLine.size()];
            for (int j = 0; j < currentConnectedLine.size(); j++) {
                xValues[j] = currentConnectedLine.get(j).x;
                yValues[j] = currentConnectedLine.get(j).y;
            }
            renderCanvas.getGraphicsContext2D().setStroke(graphColor);
            renderCanvas.getGraphicsContext2D().strokePolyline(xValues,yValues,xValues.length);
        }
    }

}
