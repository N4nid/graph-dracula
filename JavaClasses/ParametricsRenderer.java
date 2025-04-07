import javafx.scene.*;
import java.util.ArrayList;

public class ParametricsRenderer {
  double a = 0;
  double b = 8*Math.PI;
  int stepNum = 2500;
  double DeltaT = (Math.abs(a)+Math.abs(b))/stepNum;
  RenderValues renderValues;
  ArrayList<EquationTree> parametrics = new ArrayList<EquationTree>();

  private ApplicationController controller;
  
  public ParametricsRenderer(RenderValues renderValues, ApplicationController controller) {
    this.controller = controller;
    this.renderValues = renderValues;
  }
  
  public double calculateX(double t) {
    //return 3*Math.cos(t);            //circle
    //return 4*Math.cos(t)*Math.sin(4*t);            //Flower
    //return 5*Math.cos(5*t);                 //Squigle
    //return Math.sqrt(2)*Math.pow(Math.sin(t),3);            //Heart
    //return t;                     //Parabola
    //return t*Math.cos(t)/5;         //Spiral
    //return t+Math.sin(2*t);         //Chain
    return Math.sin(t)*(Math.pow(2.71828,Math.cos(t))-2*Math.cos(4*t)-Math.pow(Math.sin(1/12*t),5));      //Butterfly
  }
  
  public double calculateY(double t) {
    //return 3*Math.sin(t);              //circle
    //return 4*Math.sin(t)*Math.sin(4*t);            //Flower
    //return 5*Math.sin(4*t);                  //Squigle
    //return -Math.pow(Math.cos(t),3)-Math.pow(Math.cos(t),2)+2*Math.cos(t);   //Heart
    //return t*t;                   //Parabola
    //return t*Math.sin(t)/5;         //Spiral
    //return t+Math.sin(3*t);         //Chain
    return Math.cos(t)*(Math.pow(2.71828,Math.cos(t))-2*Math.cos(4*t)-Math.pow(Math.sin(1/12*t),5));      //Butterfly
  }
  
  private ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> calculateParametrics() {
    //zeichnet neue Funktionen aus negPosMaps in eine Pixelmap
    ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> returnParametricsLines = new ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>>();
    double minLineLenght = 2;
    Variable[] customVars = controller.customVarList.getAllCustomVars();
    EquationTree[] existingFunctions = controller.getAllFunctions();
    for (int i = 0; i < parametrics.size(); i++) {
      a = parametrics.get(i).intervalStart.calculate(new TwoDVec<Double>(0.0,0.0),customVars,existingFunctions);
      b = parametrics.get(i).intervalEnd.calculate(new TwoDVec<Double>(0.0,0.0),customVars,existingFunctions);
      ArrayList<TwoDVec<TwoDVec<Double>>> linePoints = new ArrayList<TwoDVec<TwoDVec<Double>>>();
      double t = a;
      TwoDVec<Double> lastGraphPoint = renderValues.realCoordToScreenCoord(parametrics.get(i).calculateParametrics(t,controller.customVarList.getAllCustomVars(), controller.getAllFunctions())); //new TwoDVec<Double>(calculateX(t)/renderValues.zoom.x+renderValues.midpoint.x,calculateY(t)/renderValues.zoom.y+renderValues.midpoint.y);
      t += DeltaT;
      while (t <= b) { 
        TwoDVec<Double> coords = renderValues.realCoordToScreenCoord(parametrics.get(i).calculateParametrics(t,controller.customVarList.getAllCustomVars(), controller.getAllFunctions())); //new TwoDVec<Double>(calculateX(t)/renderValues.zoom.x+renderValues.midpoint.x,calculateY(t)/renderValues.zoom.y+renderValues.midpoint.y);
        if (Math.sqrt(Math.pow(coords.x-lastGraphPoint.x,2)+Math.pow(coords.y-lastGraphPoint.y,2)) > minLineLenght) {      //0.1:=unitDistance
          linePoints.add(new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>(lastGraphPoint.x,lastGraphPoint.y),new TwoDVec<Double>(coords.x,coords.y)));
          lastGraphPoint.setPos((double)coords.x,(double)coords.y);
        }
        t += DeltaT;
      } // end of while
      //System.out.println(""+linePoints.size());
      returnParametricsLines.add(linePoints);
    }
    return returnParametricsLines;
  }

  
  public ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> calculateParametricsLinePoints(ArrayList<EquationTree> parametrics) {
    this.parametrics = parametrics;
    return calculateParametrics();
  }
}