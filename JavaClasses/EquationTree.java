import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import java.util.ArrayList;

import java.util.ArrayList;

public class EquationTree{
  
  public EquationNode root;
  public Color graphColor = Color.BLACK;
  public boolean isFunction = false;
  public boolean isParametric = false;
  public String name;
  
  public EquationTree(byte rootState, String rootValue) {
    this.root = new EquationNode(rootState,rootValue);
  }
  
  public EquationTree(byte rootState, double rootValue) {
    this.root = new EquationNode(rootState,rootValue);
  }
  
  public EquationTree(EquationNode root,String name, boolean isFunction) {
    this.name = name;
    this.isFunction = isFunction;
    this.root = root;
  }
  
  public EquationTree(EquationNode root) {
    this.root = root;
  }
  
  public EquationTree() {}
  
  public double calculate(TwoDVec<Double> coordinates, Variable[] customVariables, EquationTree[] existingEquations) {
    return root.calculate(coordinates,customVariables);
  }
  
  public TwoDVec<Double> calculateParametrics(double t, Variable[] parameters) {
    return (new TwoDVec<Double>(root.left.calculateParametric(t,parameters),root.right.calculateParametric(t,parameters)));
  }
  
  
}
