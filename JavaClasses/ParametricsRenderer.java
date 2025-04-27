import java.util.ArrayList;

public class ParametricsRenderer {
  int stepNum = 2500;
  
  RenderValues renderValues;
  ArrayList<EquationTree> parametrics = new ArrayList<EquationTree>();
  
  private ApplicationController controller;
  
  public ParametricsRenderer(RenderValues renderValues, ApplicationController controller) {
    this.controller = controller;
    this.renderValues = renderValues;
  }
  
  public ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> calculateParametricsLinePoints(ArrayList<EquationTree> parametrics,Variable[] customVars,EquationTree[] existingFunctions) {
    this.parametrics = parametrics;
    ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> returnParametricsLines = new ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>>();
    double minLineLenght = 2;
    for (int i = 0; i < parametrics.size(); i++) {
      double a = parametrics.get(i).intervalStart.calculate(new TwoDVec<Double>(0.0,0.0),customVars,existingFunctions);
      double b = parametrics.get(i).intervalEnd.calculate(new TwoDVec<Double>(0.0,0.0),customVars,existingFunctions);
      double DeltaT = (Math.abs(a)+Math.abs(b))/stepNum;
      ArrayList<TwoDVec<TwoDVec<Double>>> linePoints = new ArrayList<TwoDVec<TwoDVec<Double>>>();
      double t = a;
      TwoDVec<Double> lastGraphPoint = parametrics.get(i).calculateParametrics(t,controller.customVarList.getAllCustomVars(), controller.getAllFunctions());
      t += DeltaT;
      while (t <= b) { 
        TwoDVec<Double> coords = parametrics.get(i).calculateParametrics(t,controller.customVarList.getAllCustomVars(), controller.getAllFunctions()); //new TwoDVec<Double>(calculateX(t)/renderValues.zoom.x+renderValues.midpoint.x,calculateY(t)/renderValues.zoom.y+renderValues.midpoint.y);
        linePoints.add(new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)lastGraphPoint.x,(double)lastGraphPoint.y),new TwoDVec<Double>((double)coords.x,(double)coords.y)));
        lastGraphPoint.setPos((double)coords.x,(double)coords.y);
        t += DeltaT;
      } // end of while
      returnParametricsLines.add(linePoints);
    }
    return returnParametricsLines;
  }
}