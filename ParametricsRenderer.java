import javafx.scene.*;
import java.util.ArrayList;

public class ParametricsRenderer {
  double a = 0;
  double b = 6*Math.PI;
  int stepNum = 10000;
  double DeltaT = (a+b)/stepNum;
  RenderValues renderValues;
  Renderer renderer;
  ArrayList<EquationTree> parametrics = new ArrayList<EquationTree>();
  
  public TwoDVec<Double> lastPos = new TwoDVec<Double>(-1.0, -1.0);
  public TwoDVec<Double> lastZoom = new TwoDVec<Double>(-1.0, -1.0);
  ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> parametricLineCache = new ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>>();
  
  public ParametricsRenderer(RenderValues renderValues, Renderer renderer) {
    this.renderValues = renderValues;
    this.renderer = renderer;
  }
  
  public double calculateX(double t) {
    //return 3*Math.cos(t);
    return Math.cos(t)*Math.sin(4*t);
    //return t;
  }
  
  public double calculateY(double t) {
    //return 3*Math.sin(t);
    return Math.sin(t)*Math.sin(4*t);
    //return t*t;
  }
  
  private ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> calculateNewParametrics(boolean overrideCache) {
    //zeichnet neue Funktionen aus negPosMaps in eine Pixelmap
    ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> returnParametricsLines = new ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>>();
    double unitDistanceX = renderer.coordinateSystemRenderer.unitDistanceX;
    double unitDistanceY = renderer.coordinateSystemRenderer.unitDistanceY;
    double minLineLenght = Math.sqrt(Math.pow(unitDistanceX,2)+Math.pow(unitDistanceY,2))/1000;
    System.out.println(""+unitDistanceX+", "+unitDistanceY+", "+minLineLenght);
    for (int i = 0; i < parametrics.size(); i++) {
      ArrayList<TwoDVec<TwoDVec<Double>>> linePoints = new ArrayList<TwoDVec<TwoDVec<Double>>>();
      double t = a;
      TwoDVec<Double> lastGraphPoint = new TwoDVec<Double>(calculateX(t)*unitDistanceX+renderValues.midpoint.x,calculateY(t)*unitDistanceY+renderValues.midpoint.y);
      t += DeltaT;
      while (t <= b) { 
        TwoDVec<Double> coords = new TwoDVec<Double>(calculateX(t)*unitDistanceX+renderValues.midpoint.x,calculateY(t)*unitDistanceY+renderValues.midpoint.y);
        if (Math.sqrt(Math.pow(coords.x-lastGraphPoint.x,2)+Math.pow(coords.y-lastGraphPoint.y,2)) > minLineLenght) {      //0.1:=unitDistance
          linePoints.add(new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>(lastGraphPoint.x,lastGraphPoint.y),new TwoDVec<Double>(coords.x,coords.y)));
          lastGraphPoint.setPos((double)coords.x,(double)coords.y);
        }
        t += DeltaT;
      } // end of while
      returnParametricsLines.add(linePoints);
    }
    if (overrideCache) {
      parametricLineCache = returnParametricsLines;
    }
    return returnParametricsLines;
  }
  
  public void moveGraphs(double DeltaX, double DeltaY) {
    for (int i = 0; i < parametricLineCache.size(); i++) {
      for (int j = 0; j < parametricLineCache.get(i).size(); j++) {
        parametricLineCache.get(i).get(j).x.x += DeltaX;
        parametricLineCache.get(i).get(j).x.y += DeltaY;
        parametricLineCache.get(i).get(j).y.x += DeltaX;
        parametricLineCache.get(i).get(j).y.y += DeltaY;
      }
    }
  }
  
  public ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> calculateParametricsLinePoints(ArrayList<EquationTree> parametrics) {
    if (lastZoom.x != renderValues.zoom.x || lastZoom.y != renderValues.zoom.y) {
      this.parametrics = parametrics;
      this.lastZoom = new TwoDVec<Double>(renderValues.zoom.x, renderValues.zoom.y);
      this.lastPos = new TwoDVec<Double>(renderValues.midpoint.x, renderValues.midpoint.y);
      return calculateNewParametrics(true);
    }
    if(lastPos.x != renderValues.midpoint.x || lastPos.y != renderValues.midpoint.y) {
      double DeltaX = (renderValues.midpoint.x - lastPos.x);
      double DeltaY = (renderValues.midpoint.y - lastPos.y);
      this.lastZoom = new TwoDVec<Double>(renderValues.zoom.x, renderValues.zoom.y);
      this.lastPos = new TwoDVec<Double>(renderValues.midpoint.x, renderValues.midpoint.y);
      moveGraphs(DeltaX,DeltaY);
      return parametricLineCache;
    }    
    return parametricLineCache;
  }
}