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
  public TwoDVec<Double> xRange;
  public String name;
  
  public EquationTree(EquationNode root,String name, boolean isFunction) {
    this.name = name;
    this.isFunction = isFunction;
    this.root = root;
  }

  public EquationTree(EquationNode root,String name, boolean isFunction, TwoDVec<Double> xRange) {
    this.name = name;
    this.isFunction = isFunction;
    this.root = root;
    this.xRange = xRange;
  }
  
  public EquationTree(EquationNode root) {
    this.root = root;
  }
  
  public EquationTree() {}
  
  public double calculate(TwoDVec<Double> coordinates, Variable[] customVariables, EquationTree[] existingFunctions) {
    if (xRange != null && (xRange.x >= coordinates.x || xRange.y <= coordinates.x)) {
      return Double.NaN;
    }
    return root.calculate(coordinates,customVariables,existingFunctions);
  }
  
  public TwoDVec<Double> calculateParametrics(double t, Variable[] parameters) {
    TwoDVec<Double> result = (new TwoDVec<Double>(root.left.calculateParametric(t,parameters),root.right.calculateParametric(t,parameters)));
    if (xRange != null && (xRange.x >= result.x || xRange.y <= result.x)) {
      return new TwoDVec<Double>(Double.NaN,Double.NaN);
    }
    return result;
  }
  
  
}
