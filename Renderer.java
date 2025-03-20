import javafx.scene.canvas.Canvas;

import java.util.ArrayList;

public class Renderer {
    public Canvas mainCanvas;
    public Renderer() {
        mainCanvas = new Canvas();
    }

    public void renderLines(ArrayList<TwoDVec<TwoDVec<Double>>> lines) {
        for (int i = 0; i < lines.size(); i++) {
            TwoDVec<TwoDVec<Double>> currentLine = lines.get(i);
            mainCanvas.getGraphicsContext2D().strokeLine(currentLine.x.x,currentLine.x.y,currentLine.y.x,currentLine.y.y);
        }
    }
}
