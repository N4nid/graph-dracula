import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import java.util.ArrayList;

import java.util.ArrayList;

public class EquationTree{

  public boolean isVisible = true;

  public EquationNode root;
  public Color graphColor = Color.BLACK;
  public boolean isFunction = false;
  public boolean isParametric = false;
  public String name;
  public CondtionTree rangeCondition;
  
  public EquationTree(EquationNode root,String name, boolean isFunction) {
    this.name = name;
    this.isFunction = isFunction;
    this.root = root;
  }

  public EquationTree(EquationNode root,String name, boolean isFunction, TwoDVec<Double> xRange) {
    this.name = name;
    this.isFunction = isFunction;
    this.root = root;
    this.rangeCondition = new CondtionTree(xRange.x, xRange.y);
  }
  
  public EquationTree(EquationNode root) {
    this.root = root;
  }
  
  public EquationTree() {}
  
  public double calculate(TwoDVec<Double> coordinates, Variable[] customVariables, EquationTree[] existingFunctions) {
    if (rangeCondition != null && !rangeCondition.checkCondition(coordinates,customVariables,existingFunctions)) {
      return Double.NaN;
    }
    return root.calculate(coordinates,customVariables,existingFunctions);
  }
  
  public TwoDVec<Double> calculateParametrics(double t, Variable[] customVariables) {
    TwoDVec<Double> result = (new TwoDVec<Double>(root.left.calculateParametric(t,customVariables),root.right.calculateParametric(t,customVariables)));
    if (rangeCondition != null && !rangeCondition.checkCondition(result,customVariables,null)) {
      return new TwoDVec<Double>(Double.NaN,Double.NaN);
    }
    return result;
  }
  
  
}
